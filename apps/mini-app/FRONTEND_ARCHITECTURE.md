# Mini App frontend architecture

- `app` — composition root, router, providers and layouts.
- `features` — user actions and isolated use-cases (authentication, account access, theme).
- `entities` — domain models, domain API and entity-specific UI.
- `widgets` — reusable composite UI blocks.
- `pages` — route-level composition only.
- `shared/api` — transport and generic API infrastructure. It never renders notifications.
- `shared/errors` — the single exception normalization and handling point.
- `shared/notifications` — the single notification publication point.
- `shared/i18n/locales` — one file per language.
- `shared/styles` — base, components, pages, notifications and responsive rules.

All asynchronous UI actions should be executed through `executeAction`. API code throws typed errors and never shows UI. `handleError` is the only place that converts exceptions into user-facing error notifications. Informational messages are created only through `notifications.info/success/warning`.
