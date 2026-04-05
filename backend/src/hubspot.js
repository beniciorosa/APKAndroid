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
            { propertyName: 'dealstage', operator: 'EQ', value: CLOSED_WON_STAGE },
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

// Busca nome do owner (com cache individual por id, TTL longo).
async function getOwnerName(ownerId) {
  if (!ownerId) return 'Sem vendedor';

  const cacheKey = `owner:${ownerId}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  try {
    const { data } = await axios.get(
      `${HUBSPOT_BASE}/crm/v3/owners/${ownerId}`,
      { headers: authHeaders() }
    );
    const name =
      [data.firstName, data.lastName].filter(Boolean).join(' ') ||
      data.email ||
      `Owner ${ownerId}`;
    cache.set(cacheKey, name, 60 * 60 * 1000); // 1h
    return name;
  } catch (err) {
    return `Owner ${ownerId}`;
  }
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

  // Resolve nomes em paralelo
  const sellers = await Promise.all(
    [...byOwner.values()].map(async (s) => ({
      ...s,
      name: await getOwnerName(s.ownerId),
    }))
  );

  sellers.sort((a, b) => b.total - a.total);
  return { total, sellers };
}

async function getRevenue(period) {
  const cacheKey = `revenue:${period.from.getTime()}:${period.to.getTime()}`;
  const cached = cache.get(cacheKey);
  if (cached) return cached;

  const deals = await searchClosedWonDeals(period.from, period.to);
  const agg = await aggregateDeals(deals);

  const result = {
    total: agg.total,
    sellers: agg.sellers,
    dealCount: deals.length,
    updatedAt: new Date().toISOString(),
  };

  cache.set(cacheKey, result, 5 * 60 * 1000); // 5 min
  return result;
}

module.exports = { getRevenue };
