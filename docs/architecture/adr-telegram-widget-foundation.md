# ADR — Telegram Widget UI foundation

Status: **experimental, opt-in**. Owner: telegram-bot-service. Date: 2026-11-09.

## Decision

Mnemos maintains three presentation surfaces: Mini App, the existing text bot and Telegram widgets. Business rules and persistence remain in their existing bounded services. The Telegram service hosts both text and widget adapters and calls the same internal APIs; it must not query other services' databases.

A widget is a versioned, self-contained HTML document rendered by Telegram, not a React Mini App. UI rendering belongs to presentation components; orchestration belongs to application use cases; HTTP and Redis operations belong to outbound adapters.

### First increment

- The /widget command can send a read-only dashboard with the current account, balances and daily reward status.
- Data comes from existing AccountGateway, AccountBalanceGateway and DailyRewardGateway implementations.
- Widget HTML is escaped, bounded by 32,768 UTF-8 bytes and 4,096 zlib-compressed bytes.
- Inline keyboard shortcuts open the existing text menus. They are not widget queries.
- A rejected widget falls back to the current text menu. Unsupported clients can still use the text bot and Mini App.
- Configuration defaults to disabled: mnemos.telegram-bot.widgets.enabled=false. Enable only after tests on supported Telegram clients.
- The original menu command and menu Redis sessions are unchanged. No new service or database migration is introduced.

This is a read-only integration slice. It must not be described as a completed interactive Widget UI.

### Explicitly not implemented yet

- WidgetQuery inbound updates, answerWidgetQuery and editMessageWidgetData.
- Per-widget sessions, pending text input and multi-widget state synchronization.
- Privileged mutations, finance, account-role editing and asynchronous operations.
- Shared widgets, forwards and inline-mode widgets.

Pengrad 10.1.0 currently lacks the Bot API 10.4 widget update model. Adding only an HTTP client for sendWidget is insufficient for incoming widget interactions. Do not set allow_queries=true until the update transport is upgraded and has tested retry/ack semantics.

## Next architectural increment

1. Add a protocol adapter that supports WidgetQuery while preserving the current getUpdates offset and existing command, callback and payment handling. Never run two independent polling consumers on the same bot token.
2. Add private actor validation (chat type, chat ID, from ID), deny shared/inline widget queries until supported, and answer each valid query within 15 seconds.
3. Introduce a Redis session key scoped to bot, chat and widget message ID, with versioned state and TTL. Do not reuse the one-active-message menu session.
4. Implement short, allowlisted action codes, server-side authorization, idempotency and concurrency checks.
5. Keep authoritative balances, permissions and subscriptions in their domain services. The widget contains only a compact presentation snapshot, under the 1024-byte data cap.
6. Add an input coordinator for text messages tied to a specific widget action and reply, rather than consuming unrelated chat messages.
7. Publish data changes through bounded, coalesced editMessageWidgetData updates. Failed UI refreshes must not roll back successful business operations.
8. Introduce feature-by-feature rollout, integration tests, client compatibility tests and kill switches.

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

Production currently allows widgets only in private bot chats. The first version sets allow_queries=false and uses existing inline keyboard callbacks underneath the widget. Telegram transports may reject unsupported or invalid HTML; the bot retains the legacy text fallback.

A production rollout requires successful backend CI, independent inspection of the generated HTML in Telegram Widget Sandbox, a real Telegram test-client run, and a canary before wider enablement.
