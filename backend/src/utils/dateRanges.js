// Resolve um período em { from, to } como timestamps ISO (UTC).
// O HubSpot aceita milissegundos epoch nos filtros de data.

function startOfDay(d) {
  const x = new Date(d);
  x.setHours(0, 0, 0, 0);
  return x;
}

function endOfDay(d) {
  const x = new Date(d);
  x.setHours(23, 59, 59, 999);
  return x;
}

function resolvePeriod(period, fromStr, toStr) {
  const now = new Date();

  if (period === 'this-month') {
    const from = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0, 0);
    const to = now;
    return { from, to, label: 'Este mês' };
  }

  if (period === 'last-30-days') {
    const from = startOfDay(new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000));
    const to = now;
    return { from, to, label: 'Últimos 30 dias' };
  }

  if (period === 'custom') {
    if (!fromStr || !toStr) {
      throw new Error('custom period requer "from" e "to" (YYYY-MM-DD)');
    }
    const from = startOfDay(new Date(fromStr));
    const to = endOfDay(new Date(toStr));
    if (isNaN(from) || isNaN(to)) {
      throw new Error('datas inválidas em "from"/"to"');
    }
    if (from > to) {
      throw new Error('"from" deve ser anterior ou igual a "to"');
    }
    return { from, to, label: `${fromStr} a ${toStr}` };
  }

  throw new Error(`período inválido: ${period}. Use this-month | last-30-days | custom`);
}

module.exports = { resolvePeriod };
