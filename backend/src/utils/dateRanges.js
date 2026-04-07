// Resolve um período em { from, to } como timestamps.
// Usa fuso horário de São Paulo (UTC-3) pra "hoje" bater com o Brasil.

const TZ_OFFSET_MS = -3 * 60 * 60 * 1000; // UTC-3 (Brasília)

function nowBR() {
  // Retorna Date ajustado pra hora do Brasil
  const utc = new Date();
  return new Date(utc.getTime() + TZ_OFFSET_MS);
}

function startOfDayBR(d) {
  // d já está em "hora BR" — zera horas e converte de volta pra UTC
  const x = new Date(d);
  x.setUTCHours(0, 0, 0, 0);
  return new Date(x.getTime() - TZ_OFFSET_MS); // volta pra UTC real
}

function endOfDayBR(d) {
  const x = new Date(d);
  x.setUTCHours(23, 59, 59, 999);
  return new Date(x.getTime() - TZ_OFFSET_MS);
}

function resolvePeriod(period, fromStr, toStr) {
  const brNow = nowBR();
  const utcNow = new Date();

  if (period === 'today') {
    const from = startOfDayBR(brNow);
    const to = utcNow;
    return { from, to, label: 'Hoje' };
  }

  if (period === 'this-month') {
    const monthStart = new Date(Date.UTC(brNow.getUTCFullYear(), brNow.getUTCMonth(), 1, 0, 0, 0, 0));
    const from = new Date(monthStart.getTime() - TZ_OFFSET_MS); // 00:00 BR em UTC
    const to = utcNow;
    return { from, to, label: 'Este mês' };
  }

  if (period === 'last-30-days') {
    const thirtyAgo = new Date(brNow);
    thirtyAgo.setUTCDate(thirtyAgo.getUTCDate() - 30);
    const from = startOfDayBR(thirtyAgo);
    const to = utcNow;
    return { from, to, label: 'Últimos 30 dias' };
  }

  if (period === 'custom') {
    if (!fromStr || !toStr) {
      throw new Error('custom period requer "from" e "to" (YYYY-MM-DD)');
    }
    // Datas customizadas: tratadas como datas em Brasília
    const fromParts = fromStr.split('-').map(Number);
    const toParts = toStr.split('-').map(Number);
    const fromBR = new Date(Date.UTC(fromParts[0], fromParts[1] - 1, fromParts[2]));
    const toBR = new Date(Date.UTC(toParts[0], toParts[1] - 1, toParts[2]));
    const from = new Date(fromBR.getTime() - TZ_OFFSET_MS); // 00:00 BR
    const to = new Date(toBR.getTime() + 24 * 60 * 60 * 1000 - 1 - TZ_OFFSET_MS); // 23:59:59.999 BR
    if (isNaN(from) || isNaN(to)) {
      throw new Error('datas inválidas em "from"/"to"');
    }
    if (from > to) {
      throw new Error('"from" deve ser anterior ou igual a "to"');
    }
    return { from, to, label: `${fromStr} a ${toStr}` };
  }

  throw new Error(`período inválido: ${period}. Use today | this-month | last-30-days | custom`);
}

module.exports = { resolvePeriod };
