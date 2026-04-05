# Dashboard Faturamento — App Android + Backend Vercel

App Android nativo que mostra o faturamento da empresa (dados do HubSpot), com widget de home screen configurável por período.

**Stack:**
- Backend: Node.js + Express, hospedado no Vercel
- App: Kotlin + Jetpack Compose + Glance (widget) + Retrofit + Hilt
- Fonte de dados: HubSpot CRM API v3 (deals closedwon)

---

## 1. Backend (Vercel)

### Estrutura
```
backend/
├── api/index.js            # entry point Vercel (Express app)
├── src/
│   ├── hubspot.js          # cliente HubSpot
│   ├── cache.js            # cache em memória (TTL 5min)
│   └── utils/dateRanges.js
├── vercel.json
├── package.json
└── .env.example
```

### Environment Variables no Vercel
Já configuradas no seu projeto:
- `HUBSPOT_ACCESS_TOKEN` — Private App Token (escopos: `crm.objects.deals.read`, `crm.objects.owners.read`)
- `CURRENCY` (opcional, default `BRL`)
- `CLOSED_WON_STAGE` (opcional, default `closedwon`)

### Deploy
O deploy é automático: cada push na branch `main` do GitHub aciona um novo deploy no Vercel.

### Endpoints disponíveis
- `GET https://apk-android.vercel.app/api/health` — ping
- `GET https://apk-android.vercel.app/api/revenue?period=this-month` — total
- `GET https://apk-android.vercel.app/api/revenue/by-seller?period=this-month` — por vendedor

Períodos aceitos: `this-month`, `last-30-days`, `custom` (com `from`, `to` em `YYYY-MM-DD`).

### Testar o backend
```bash
curl https://apk-android.vercel.app/api/health
curl "https://apk-android.vercel.app/api/revenue/by-seller?period=this-month"
```

### Rodar local (opcional)
```bash
cd backend
npm install
cp .env.example .env          # preencher HUBSPOT_ACCESS_TOKEN
npm start                     # http://localhost:3000
```

---

## 2. App Android

### Abrir no Android Studio
1. Abra o Android Studio → **Open** → selecione a pasta `android/`.
2. Aguarde o Gradle sincronizar (baixa dependências).
3. Conecte o celular via USB (com **depuração USB** ativada) ou crie um emulador.
4. Clique em **Run ▶**. O app é instalado e aberto.

### Gerar o APK de instalação
```bash
cd android
./gradlew assembleDebug
# APK gerado em: android/app/build/outputs/apk/debug/app-debug.apk
```
No Windows (sem Gradle instalado globalmente), use o **wrapper** gerado pelo Android Studio (`gradlew.bat`) — ele aparece na pasta `android/` após o primeiro sync.

### Instalar o APK no celular
1. Transfira `app-debug.apk` para o celular (WhatsApp próprio, Google Drive, cabo USB).
2. Abra o arquivo no celular → **Instalar**.
3. Pode pedir para ativar **"Instalar apps de fontes desconhecidas"** nas configurações.

### Usar o widget
1. Segure em uma área vazia da tela inicial → **Widgets**.
2. Procure por **"Faturamento"** → arraste para a tela.
3. Na tela de configuração, escolha o período (Este mês / Últimos 30 dias / Personalizado).
4. Toque em **Salvar e adicionar widget**.
5. O widget atualiza automaticamente a cada 30 minutos. Tocar abre o app.

---

## 3. Estrutura do projeto

```
APKAndroid/
├── backend/              # API Node.js (Vercel)
├── android/              # Projeto Android Studio
│   └── app/src/main/
│       ├── java/com/empresa/dashboard/
│       │   ├── data/         # Retrofit + Repository + Hilt
│       │   ├── ui/           # Tela dashboard (Compose)
│       │   └── widget/       # Widget Glance + Config Activity
│       └── res/              # Strings, cores, ícones, widget XML
└── README.md
```

---

## 4. Próximos passos sugeridos

- **Autenticação**: adicionar API key simples no header (`X-API-Key`) para proteger o backend público.
- **Gráficos**: adicionar gráfico de linha (histórico) usando `compose-charts`.
- **Ícone personalizado**: substituir `ic_launcher_foreground.xml` pelo logo da empresa.
- **Notificações**: alerta diário com faturamento do dia via WorkManager.
- **Mais métricas**: pipeline, metas vs realizado, top produtos.

---

## 5. Troubleshooting

| Problema | Solução |
|---|---|
| Widget mostra "—" | Abra o app uma vez para forçar o primeiro fetch, ou aguarde 30min do WorkManager. |
| App mostra erro de conexão | Verifique se `https://apk-android.vercel.app/api/health` retorna `{"ok":true}` no navegador. |
| Backend retorna `HUBSPOT_ACCESS_TOKEN não configurado` | Verifique env var no Vercel (Settings → Environment Variables) e faça redeploy. |
| Gradle sync falha | Rode `./gradlew --refresh-dependencies` ou File → Invalidate Caches no Android Studio. |
