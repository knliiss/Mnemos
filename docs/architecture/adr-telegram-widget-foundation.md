# ADR — Telegram Widget UI foundation

Status: **experimental, opt-in; dashboard, notifications, account selection, daily rewards and event-feed WidgetQuery implemented in draft PR, still unverified against a real client**. Owner: telegram-bot-service. Date: 2026-11-09.

## Decision

Mnemos maintains three presentation surfaces: Mini App, the existing text bot and Telegram widgets. Business rules and persistence remain in their existing bounded services. The Telegram service hosts both text and widget adapters and calls the same internal APIs; it must not query other services' databases.

A widget is a versioned, self-contained HTML document rendered by Telegram, not a React Mini App. UI rendering belongs to presentation components; orchestration belongs to application use cases; HTTP and Redis operations belong to outbound adapters.

### First increment

- The /widget command sends an interactive dashboard with account, balances, and a daily claim action.
- /notifications sends an interactive subscriptions widget with ping settings, item-drop pagination and deletion. Creation/editing of complex rules remains in the legacy chat menu.
- /events (without a text search query) sends a compact event feed widget with filter tabs, refresh, and cursor-paginated next-page messages. /events with a search query still uses the text bot.
- /account sends an account selector widget with active account, account balances, enabled-account selection and pagination. Creation, joining and advanced administration remain in the existing text menu.
- /daily sends a reward widget with claim and streak restore actions backed by the existing DailyRewardGateway; account eligibility is checked again for each request.
- Data comes from existing AccountGateway, AccountBalanceGateway and DailyRewardGateway implementations.
- Widget HTML is self-contained, bounded by 32,768 UTF-8 bytes and 4,096 zlib-compressed bytes, and sends user data only via JSON chat_data and textContent.
- Widget buttons use widget queries; inline keyboard shortcuts still open the existing text menus for complex inputs.
- A rejected widget falls back to the current text menu. Unsupported clients can still use the text bot and Mini App.
- Configuration defaults to disabled: mnemos.telegram-bot.widgets.enabled=false. Enable only after tests on supported Telegram clients.
- The original menu command and menu Redis sessions are unchanged. Widget sessions use separate, versioned Redis keys scoped to bot, chat, and message ID. No new microservice or database migration is introduced.

This is a functional but incomplete Widget UI integration slice. It must not be described as a completed parity migration.

### Explicitly not implemented yet

- Widget-specific pending text input; live cross-widget synchronization and message-level refresh scheduling.
- Privileged mutations, finance, account-role editing and asynchronous operations.
- Shared widgets, forwards and inline-mode widgets.

Pengrad 10.1.0 lacks the Bot API 10.4 widget update model. When the widget flag is enabled, the service therefore replaces Pengrad polling with a single JSON getUpdates poller that delegates legacy updates to the existing consumer and processes widget_query directly. Never run both pollers at the same time.

## Next architectural increment

1. Add widget-specific text input sessions tied to replies; keep existing legacy menu input as fallback.
2. Build widget screen versioning, update subscriptions, and refresh coalescing for multiple live cards.
3. Make slow actions asynchronous so every widget query receives an acknowledgement within the 15-second deadline.
4. Extend feature parity beyond the initial event feed to profiles, wallet, bets, personalization and administration.
5. Introduce feature-by-feature rollout, integration tests, client compatibility tests and kill switches.

## Rules for code quality

- One primary class or type per Kotlin file; closely related data members may remain together when they form one stable contract.
- No Spring, HTTP clients, Telegram SDK types or persistence APIs in application command models and domain policies.
- Inbound adapters translate transport input into typed commands; outbound adapters implement ports. Do not duplicate business rules in the Telegram service.
- UI renderers are pure, and never perform network requests or change state.
- Explicit command and query types are preferred to opaque maps for business operations.
- No arbitrary worker method/path/body forwarding from a Telegram widget. Use an allowlisted, typed bot-management API.
- Respect existing Kafka inbox/outbox mechanisms and service contract versioning.
- New behavior requires unit tests, integration coverage at its boundary, and feature flag / fallback behavior.

## Operational notes

Bot API 10.4 reference: https://core.telegram.org/bots/api
Widget runtime: https://core.telegram.org/bots/widgets

Production currently allows widgets only in private bot chats. The current interactive widgets set allow_queries=true and process WidgetQuery through one widget-aware polling consumer. Inline keyboard callbacks remain as a secondary text-menu path. Telegram transports may reject unsupported or invalid HTML; the bot retains the legacy text fallback.

A production rollout requires successful backend CI, independent inspection of the generated HTML in Telegram Widget Sandbox, a real Telegram test-client run, and a canary before wider enablement.

## Implemented interaction contract

- Dashboard actions: `w1:refresh`, `w1:claim`, `w1:sub:open`, `w1:evt:open`, `w1:acct:open`, `w1:daily:open`.
- Subscriptions: `w1:sub:refresh`, `w1:ping:on`, `w1:ping:off`, `w1:next:<expected-page>`, `w1:prev:<expected-page>`, `w1:del:<subscription-uuid>`.
- Accounts: `w1:acct:refresh`, `w1:acct:next:<expected-page>`, `w1:acct:prev:<expected-page>`, `w1:acct:select:<account-uuid>`. An account selection must be on the current visible page, active and permitted by account-service. A change of active account invalidates prior widgets with an older account context.
- Daily: `w1:daily:refresh`, `w1:daily:claim`, `w1:daily:restore`. Repeated claims and unavailable restores avoid duplicate mutations.
- Events: `w1:evt:refresh`, `w1:evt:filter:<event-type>`, `w1:evt:next:<cursor-uuid>`. A next-page command opens a new widget message, leaving the old page intact. Text-based search uses the existing bot workflow.
- Every widget_query must come from the widget owner in a private chat, match a stored message session, and preserve its original active account. Shared and inline query types are rejected.
- Ping mutations express the desired end state rather than an imperative toggle, so a redelivered update cannot flip the setting twice.
- Deletion only accepts item-drop rules visible in the currently loaded page, and delegates ownership checks to the notification service.
- The UI can receive a fresh snapshot immediately in answerWidgetQuery and asynchronously via editMessageWidgetData. Failed UI delivery must not be treated as a failed domain write.

## Open production constraints

- CI and device compatibility tests are outstanding by choice; do not enable the flag in production yet.
- Polling handover is mutually exclusive per application instance; multi-instance ownership/leader election remains necessary before scaling the bot service horizontally.
- The code currently supports dashboard, notification, events, account selection and daily-rewards widgets; other screens remain in the existing Telegram text bot and Mini App.
