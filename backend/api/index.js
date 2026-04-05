// Entry point para Vercel serverless.
// Exporta o app Express; o Vercel trata do listen.
const express = require('express');
const cors = require('cors');
const { resolvePeriod } = require('../src/utils/dateRanges');
const { getRevenue } = require('../src/hubspot');

const app = express();
const CURRENCY = process.env.CURRENCY || 'BRL';

app.use(cors());
app.use(express.json());

app.get('/api/health', (req, res) => {
  res.json({
    ok: true,
    time: new Date().toISOString(),
    hasToken: !!process.env.HUBSPOT_ACCESS_TOKEN,
  });
});

app.get('/api/revenue', async (req, res) => {
  try {
    const { period = 'this-month', from, to } = req.query;
    const range = resolvePeriod(period, from, to);
    const data = await getRevenue(range);
    res.json({
      total: data.total,
      currency: CURRENCY,
      dealCount: data.dealCount,
      period: {
        key: period,
        label: range.label,
        from: range.from.toISOString(),
        to: range.to.toISOString(),
      },
      updatedAt: data.updatedAt,
    });
  } catch (err) {
    console.error('[/api/revenue]', err.message);
    res.status(400).json({ error: err.message });
  }
});

app.get('/api/revenue/by-seller', async (req, res) => {
  try {
    const { period = 'this-month', from, to } = req.query;
    const range = resolvePeriod(period, from, to);
    const data = await getRevenue(range);
    res.json({
      total: data.total,
      currency: CURRENCY,
      dealCount: data.dealCount,
      sellers: data.sellers,
      period: {
        key: period,
        label: range.label,
        from: range.from.toISOString(),
        to: range.to.toISOString(),
      },
      updatedAt: data.updatedAt,
    });
  } catch (err) {
    console.error('[/api/revenue/by-seller]', err.message);
    const status = err.response?.status || 400;
    res.status(status).json({ error: err.message });
  }
});

app.get('/', (req, res) => {
  res.json({
    name: 'Dashboard Faturamento API',
    endpoints: ['/api/health', '/api/revenue', '/api/revenue/by-seller'],
  });
});

// Para uso local (npm start) — Vercel ignora isto
if (require.main === module) {
  require('dotenv').config();
  const PORT = process.env.PORT || 3000;
  app.listen(PORT, () => console.log(`Local: http://localhost:${PORT}`));
}

module.exports = app;
