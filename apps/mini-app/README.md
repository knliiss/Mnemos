# Mnemos Mini App

Telegram Mini App frontend built with React, TypeScript and Vite.

## Run

```bash
cp .env.example .env
npm install
npm run build
npm run dev
```

The application intentionally requires `window.Telegram.WebApp.initData`. Keycloak authentication uses PKCE and appends `telegram_init_data` to the login URL.

## Backend integration

Real endpoints currently used:

- `PUT /api/v1/users/me`
- `PATCH /api/v1/users/me/localization`
- `GET /api/v1/me/accounts`
- `GET /api/v1/me/accounts/active`
- `PUT /api/v1/me/accounts/active/{accountId}`
- `POST /api/v1/me/accounts/active/leave`
- `POST /api/v1/accounts`

Domain sections not yet present on the backend return local data inside service methods. They keep the same async service boundary, so a method body can later be replaced by `api(...)` without changing pages or components.

## Structure

- `app` — application state, routing, i18n
- `components` — reusable UI components
- `layouts` — navigation shell
- `pages` — route-level screens
- `services` — backend and local data adapters
- `types` — domain DTOs
- `styles` — design tokens and global layout
