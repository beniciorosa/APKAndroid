// Cliente HubSpot: busca deals closedwon e agrega por owner.

const axios = require('axios');
const cache = require('./cache');

const HUBSPOT_BASE = 'https://api.hubapi.com';
const CLOSED_WON_STAGE = process.env.CLOSED_WON_STAGE || 'closedwon';
function getToken() {
  // Aceita HUBSPOT_ACCESS_TOKEN (padrão Vercel) ou HUBSPOT_TOKEN (local)
  return process.env.HUBSPOT_ACCESS_TOKEN || process.env.HUBSPOT_TOKEN;
}

function authHeaders() {
  const token = getToken();
  if (!token) {
    throw new Error('HUBSPOT_ACCESS_TOKEN não configurado');
  }
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}

// Busca todos os deals closedwon no período (paginado).
async function searchClosedWonDeals(from, to) {
  const deals = [];
  let after = undefined;
  const limit = 100;

  while (true) {
    const body = {
      filterGroups: [
        {
          filters: [
            { propertyName: 'hs_is_closed_won', operator: 'EQ', value: 'true' },
            { propertyName: 'closedate', operator: 'GTE', value: from.getTime().toString() },
            { propertyName: 'closedate', operator: 'LTE', value: to.getTime().toString() },
          ],
        },
      ],
      properties: ['amount', 'closedate', 'hubspot_owner_id', 'dealname'],
      limit,
      after,
    };

    const { data } = await axios.post(
      `${HUBSPOT_BASE}/crm/v3/objects/deals/search`,
      body,
      { headers: authHeaders() }
    );

    deals.push(...data.results);

    if (data.paging && data.paging.next && data.paging.next.after) {
      after = data.paging.next.after;
    } else {
      break;
    }
  }

  return deals;
}

// Busca todos os owners de uma vez e monta um mapa {id: nome}
async function getAllOwnersMap() {
  const cacheKey = 'owners:all';
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const map = new Map();
  let after = undefined;
  try {
    while (true) {
      const url = new URL(`${HUBSPOT_BASE}/crm/v3/owners/`);
      url.searchParams.set('limit', '100');
      if (after) url.searchParams.set('after', after);
      const { data } = await axios.get(url.toString(), { headers: authHeaders() });
      for (const o of data.results) {
        const name =
          [o.firstName, o.lastName].filter(Boolean).join(' ') ||
          o.email ||
          `Owner ${o.id}`;
        map.set(String(o.id), name);
      }
      if (data.paging?.next?.after) after = data.paging.next.after;
      else break;
    }
  } catch (err) {
    console.error('[getAllOwnersMap] falha:', err.response?.status, err.message);
  }
  cache.set(cacheKey, map, 60 * 60 * 1000); // 1h
  return map;
}

function resolveOwnerName(ownersMap, ownerId) {
  if (!ownerId) return 'Sem vendedor';
  return ownersMap.get(String(ownerId)) || `Owner ${ownerId}`;
}

// Agrega deals → { total, bySeller: [{ownerId, name, total, dealCount}] }
async function aggregateDeals(deals) {
  const byOwner = new Map();
  let total = 0;

  for (const deal of deals) {
    const amount = parseFloat(deal.properties.amount || '0') || 0;
    const ownerId = deal.properties.hubspot_owner_id || null;
    total += amount;

    if (!byOwner.has(ownerId)) {
      byOwner.set(ownerId, { ownerId, total: 0, dealCount: 0 });
    }
    const agg = byOwner.get(ownerId);
    agg.total += amount;
    agg.dealCount += 1;
  }

  const sellers = [...byOwner.values()];
  sellers.sort((a, b) => b.total - a.total);
  return { total, sellers };
}

async function getRevenue(period) {
  // Cache apenas dos dados dos deals (sem nomes — nomes são resolvidos depois)
  const cacheKey = `revenue:${period.from.getTime()}:${period.to.getTime()}`;
  let aggregated = cache.get(cacheKey);
  let dealCount = cache.get(`${cacheKey}:count`);

  if (!aggregated) {
    const deals = await searchClosedWonDeals(period.from, period.to);
    aggregated = await aggregateDeals(deals);
    dealCount = deals.length;
    cache.set(cacheKey, aggregated, 5 * 60 * 1000);
    cache.set(`${cacheKey}:count`, dealCount, 5 * 60 * 1000);
  }

  // Resolve nomes sempre na hora (owners tem cache próprio de 1h)
  const ownersMap = await getAllOwnersMap();
  const sellers = aggregated.sellers.map((s) => ({
    ...s,
    name: resolveOwnerName(ownersMap, s.ownerId),
  }));

  return {
    total: aggregated.total,
    sellers,
    dealCount,
    updatedAt: new Date().toISOString(),
  };
}

// Diagnóstico: lista pipelines e stages
async function listPipelines() {
  const { data } = await axios.get(
    `${HUBSPOT_BASE}/crm/v3/pipelines/deals`,
    { headers: authHeaders() }
  );
  return data.results.map((p) => ({
    id: p.id,
    label: p.label,
    stages: p.stages.map((s) => ({
      id: s.id,
      label: s.label,
      closedWon: s.metadata?.isClosed === 'true' && s.metadata?.probability === '1.0',
      metadata: s.metadata,
    })),
  }));
}

// Diagnóstico: conta todos os deals (sem filtro) para verificar acesso
async function countAllDeals(from, to) {
  const body = {
    filterGroups: [
      {
        filters: [
          { propertyName: 'closedate', operator: 'GTE', value: from.getTime().toString() },
          { propertyName: 'closedate', operator: 'LTE', value: to.getTime().toString() },
        ],
      },
    ],
    properties: ['amount', 'dealstage', 'hs_is_closed_won', 'closedate'],
    limit: 100,
  };
  const { data } = await axios.post(
    `${HUBSPOT_BASE}/crm/v3/objects/deals/search`,
    body,
    { headers: authHeaders() }
  );
  return {
    total: data.total,
    samples: data.results.slice(0, 5).map((d) => ({
      id: d.id,
      amount: d.properties.amount,
      dealstage: d.properties.dealstage,
      hs_is_closed_won: d.properties.hs_is_closed_won,
      closedate: d.properties.closedate,
    })),
  };
}

// Diagnóstico: tenta listar owners e retorna resultado bruto
async function diagOwners() {
  try {
    const { data } = await axios.get(
      `${HUBSPOT_BASE}/crm/v3/owners?limit=10`,
      { headers: authHeaders() }
    );
    return { ok: true, count: data.results?.length || 0, sample: data.results?.slice(0, 3) };
  } catch (err) {
    return {
      ok: false,
      status: err.response?.status,
      message: err.message,
      body: err.response?.data,
    };
  }
}

module.exports = { getRevenue, listPipelines, countAllDeals, diagOwners };
