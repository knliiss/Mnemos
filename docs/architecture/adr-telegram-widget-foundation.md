# ADR — Telegram Widget UI foundation

Working context: [Telegram widget feature context](telegram-widget-feature-context.md). Review the living implementation ledger and route inventory before extending this feature.

Status: **experimental, opt-in; dashboard, notifications, account selection, daily rewards, read-only bets, wallet, SAO Online and event-feed WidgetQuery implemented in draft PR, still unverified against a real client**. Owner: telegram-bot-service. Date: 2026-11-09.

## Decision

Mnemos maintains three presentation surfaces: Mini App, the existing text bot and Telegram widgets. Business rules and persistence remain in their existing bounded services. The Telegram service hosts both text and widget adapters and calls the same internal APIs; it must not query other services' databases.

A widget is a versioned, self-contained HTML document rendered by Telegram, not a React Mini App. UI rendering belongs to presentation components; orchestration belongs to application use cases; HTTP and Redis operations belong to outbound adapters.

### First increment

- The /widget command sends an interactive dashboard with account, balances, and a daily claim action.
- /notifications sends an interactive subscriptions widget with ping settings, item-drop pagination and deletion. Creation/editing of complex rules remains in the legacy chat menu.
- /events (without a text search query) sends a compact event feed widget with filter tabs, refresh, and cursor-paginated next-page messages. /events with a search query still uses the text bot.
- /account sends an account selector widget with active account, account balances, enabled-account selection and pagination. Creation, joining and advanced administration remain in the existing text menu.
- /daily sends a reward widget with claim and streak restore actions backed by the existing DailyRewardGateway; account eligibility is checked again for each request.
- /bets sends a read-only betting widget with round odds, item races and bet history. Monetary actions remain in the established text bot flow with typed idempotency keys and amount input.
- /wallet sends a balance and transaction-history widget with paging and refresh. PRIME purchase continues through the existing Telegram Stars flow.
- /online with no arguments opens a realm and player overview widget using the existing SaoOnlineSnapshotGateway; realm navigation and refresh work in-place. Searches and full lists continue through the existing text menu.
- Data comes from existing AccountGateway, AccountBalanceGateway and DailyRewardGateway implementations.
- Widget HTML is self-contained, bounded by 32,768 UTF-8 bytes and 4,096 zlib-compressed bytes, and sends user data only via JSON chat_data and textContent.
- Widget buttons use widget queries; inline keyboard shortcuts still open the existing text menus for complex inputs.
- A rejected widget falls back to the current text menu. Unsupported clients can still use the text bot and Mini App.
- Configuration defaults to disabled: mnemos.telegram-bot.widgets.enabled=false. Enable only after tests on supported Telegram clients.
- The original menu command and menu Redis sessions are unchanged. Widget sessions use separate, versioned Redis keys scoped to bot, chat, and message ID. No new microservice or database migration is introduced.

This is a functional but incomplete Widget UI integration slice. It must not be described as a completed parity migration.

### Explicitly not implemented yet

- Full widget-native editing and mutations. Read-only event/player search now updates its originating widget and account create/join uses correlated chat prompts; advanced subscription, financial, moderation and creator flows remain unported. Live cross-widget synchronization and scheduled refresh remain unimplemented.
- Privileged mutations, finance, account-role editing and asynchronous operations.
- Shared widgets, forwards and inline-mode widgets.

Pengrad 10.1.0 lacks the Bot API 10.4 widget update model. When the widget flag is enabled, the service therefore replaces Pengrad polling with a single JSON getUpdates poller that delegates legacy updates to the existing consumer and processes widget_query directly. Never run both pollers at the same time.

## Next architectural increment

1. Extend the reply-bound event/player/account input foundation to subscriptions, invitations, roles and other typed workflows, with domain-level idempotency and durable operation receipts before monetary mutations.
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
- Daily: `w1:daily:refresh`, `w1:daily:claim`, `w1:daily:restore`.
- Bets (read-only): `w1:bet:refresh`, `w1:bet:mode:ROUNDS`, `w1:bet:mode:RACES`, `w1:bet:mode:HISTORY`.
- Wallet (read-only): `w1:wallet:refresh`, `w1:wallet:prev:<expected-page>`, `w1:wallet:next:<expected-page>`.
- SAO Online (read-only): `w1:on:refresh`, `w1:on:list`, `w1:on:realm:<realm-number>`. Realm numbers are validated against the live realm list and stored per widget message. Repeated claims and unavailable restores avoid duplicate mutations.
- Events: `w1:evt:refresh`, `w1:evt:filter:<event-type>`, `w1:evt:next:<cursor-uuid>`. A next-page command opens a new widget message, leaving the old page intact. Text-based search uses the existing bot workflow.
- Every widget_query must come from the widget owner in a private chat, match a stored message session, and preserve its original active account. Shared and inline query types are rejected.
- Ping mutations express the desired end state rather than an imperative toggle, so a redelivered update cannot flip the setting twice.
- Deletion only accepts item-drop rules visible in the currently loaded page, and delegates ownership checks to the notification service.
- The UI can receive a fresh snapshot immediately in answerWidgetQuery and asynchronously via editMessageWidgetData. Failed UI delivery must not be treated as a failed domain write.

## Open production constraints

- CI and device compatibility tests are outstanding by choice; do not enable the flag in production yet.
- Polling handover is mutually exclusive per application instance; multi-instance ownership/leader election remains necessary before scaling the bot service horizontally.
- The code currently supports dashboard, notification, events, account selection, daily rewards, read-only bets and wallet widgets; other screens remain in the existing Telegram text bot and Mini App.

## Payload and maintainability safeguards

- All user-supplied widget feed labels and account display names are truncated by **UTF-8 byte length** before being put into `chat_data`, not by UTF-16 character count. The truncation preserves complete Unicode code points and is covered by tests.
- `answerWidgetQuery` has an independent 1024-byte UTF-8 response budget. If combining the state and acknowledgement would exceed this limit, the answer contains only status and message; the separate `editMessageWidgetData` update carries the refreshed state.
- Every new widget feature has separate snapshot/query, action handler, publisher port, adapter and presentation renderer. Kotlin declarations are kept in their own files rather than growing one central router or large model file.
- The desktop/browser Mini App and the legacy Telegram menu stay available for unsupported Telegram clients and complex input workflows.

## Betting safety boundary

- Widget requests never accept arbitrary monetary amounts, raw REST paths or direct wallet operations.
- Betting data comes from the existing typed `BetGateway` and active account context. A widget from an older account cannot refresh an account-scoped bet screen after an account switch.
- The button for initiating a bet opens the existing Telegram chat flow, which already requests the amount and derives a stable idempotency key. A dedicated widget-originated input workflow will require separate reply correlation and operation-state handling.

## Wallet history security boundary

A Telegram widget does not call the public wallet-service account transaction endpoint. That endpoint depends on the authenticated web user and cannot safely be reused with service-only credentials.

The wallet module now exposes a dedicated *read-only* internal route, restricted to the `telegram-bot-service` principal. It resolves the real Telegram user's UUID and active account through a new `wallet-context` endpoint on account-service (restricted to `wallet-service`), checks the `VIEW_TRANSACTION_HISTORY` role permission and only then reads transactions for that resolved account.

The internal wallet response includes `accountId`. The bot compares that value against the active account context, and rereads the account after loading balances and history; mismatches are rejected instead of showing another account's data. A 403 response shows balances without revealing transaction entries. Wallet data is read-only: no amount or money-mutation action is accepted through the widget.

The query UI limits each transaction page to four rows. The internal API constrains page size to a maximum of four and validates page numbers. Both the financial state and account binding must be revalidated for each WidgetQuery.

## SAO Online read-only navigation

The online widget consumes the existing typed `SaoOnlineSnapshotGateway` and does not introduce a separate data collector. The widget shows at most eight realms in its overview and six players in the selected realm so it remains comfortably inside the 1024-byte state budget. It presents UTC snapshot time as supplied by the gateway and makes no claim that results are instantaneous.

The widget route is only used for the argument-free `/online` command. Realm-number and nickname search commands continue through the existing text menu. The text fallback also remains available through the widget's `Пошук гравця` callback.

## Settings and continuation additions (2026-11-10)

- `/settings` is now an opt-in WidgetQuery screen for language selection and owned notification-pack activation via the existing `SettingsGateway`; pagination, refresh and legacy menu fallback are supported.
- Dashboard WidgetQuery has `w1:settings:open`. Other settings actions are `w1:settings:refresh`, `w1:settings:lang:<language>`, `w1:settings:pack:<uuid>`, `w1:settings:next:<expected-page>`, and `w1:settings:prev:<expected-page>`.
- Accounts widgets support creating and joining via reply-bound chat prompts `w1:acct:create` and `w1:acct:join`, reusing `AccountGateway` and a Redis at-most-once prompt claim. **Domain-level idempotency is not yet guaranteed**, especially for uncertain downstream results or separate prompts; do not reuse this as a finance flow.
- Event and online search actions update the originating widget using search bound to a `v4` message session (the Redis reader accepts older `v2/v3` sessions). SAO nickname matching is shared with the text menu.
- This ADR still describes the first migration slices; the live parity checklist and latest implementation ledger are in [the feature working context](telegram-widget-feature-context.md). No production enablement or claim of full parity is authorized without feature-by-feature verification.

## Invites and notification expansion (2026-11-10)

- `/invites` and `w1:acct:invites` open an account-context-bound invitation widget with `w1:inv:refresh`, `w1:inv:next:<page>`, `w1:inv:prev:<page>`, `w1:inv:create:<expiry>`, `w1:inv:confirm:<uuid>`, `w1:inv:revoke:<uuid>`, `w1:inv:cancel`. Existing `AccountInviteGateway` checks permissions and ownership; a per-message Redis confirmation is mandatory before revocation, and repeat query IDs are claimed.
- Notification widget supports `w1:sub:booster:<type>:<on|off>`, `w1:sub:dungeon:<type>:<on|off>`, `w1:sub:global:<type>:<on|off>` and `w1:sub:raid:<base36-mask>`; the latter encodes a 28-bit bitset of locations 1–28 and uses server-side validation. Desired states are rechecked using existing `NotificationSubscriptionGateway` before mutation.
- Item-drop rule creation uses `w1:sub:drop:player`, `w1:sub:drop:item`, `w1:sub:drop:both` and reply-bound input. The combined input is `nickname;itemName`. Validation reuses existing notification domain validators.
- All code remains behind `mnemos.telegram-bot.widgets.enabled` and must not be deployed without compilation, tests and real Telegram Widget Sandbox/device validation.

## Secure public-profile migration (2026-11-10)

- Public profile lookup uses a new internal read-only contract in `contracts/profile-contracts`. Existing user-facing `/api/v1/profiles` endpoints remain behind JWT; Telegram does not call them with fabricated user credentials.
- `profile-service` serves `/internal/v1/telegram/profiles` and `/{accountId}` with its established service-key filter, calling the same public get/search application use cases. The API returns only public fields; hidden presence never includes realm or last-seen.
- Telegram's typed `ProfileServiceClient` sends `X-Mnemos-Service` and `X-Mnemos-API-Key`. The `PROFILE_SERVICE_TELEGRAM_BOT_API_KEY` and `TELEGRAM_BOT_SERVICE_PROFILE_API_KEY` environment values must match; neither secret is committed. Provision secrets before any rollout.
- `/profile` and `/profiles` open a widget with private-chat reply-bound search, paginated public results and a profile detail view. Selected profile IDs are stored in `v5` Redis sessions, with legacy `v2/v3/v4` reads preserved. Profile history has its own read-only item-drop activity widget and cursor pagination via existing event-service queries.
- Widgets must never modify profile reactions, comments or privacy settings through read-only DTOs. All future social writes require social-service-backed authenticated commands, replay safety and permission checks.
- Profile activity state uses compact serialized keys to respect Telegram's maximum 1,024-byte `chat_data`. The HTML, JSON and navigation tests are committed but not yet run in CI or a real Telegram client.
- Not finished: user social interactions, full activity, personalized visuals, user wallet, marketplace, roles, leaderboards, privileged screens and comprehensive test coverage. The feature flag must remain disabled.

## Profile activity and event-detail slices (2026-11-10)

- Public-profile detail can open an independent `PROFILE_ACTIVITY` widget. The application combines the existing public profile gateway with exact player drop filtering from the event service. Widget query cursors are checked against a fresh server-loaded page, not trusted from the client.
- The event feed's `w1:evt:detail:<index>` action resolves the index only from the current server-loaded page before opening an immutable, read-only detail widget. A new `/internal/v1/events/details/{eventId}` endpoint is restricted to the Telegram bot service principal. This does not yet transfer follow/unfollow or spotlight actions.
- Treating Telegram ID as a social service user ID is forbidden: social writes must use user/active-account resolution with explicit permissions. Read-only profile details and activity remain separate from account-scoped reactions and comments until that boundary is implemented.
- See [feature working context](telegram-widget-feature-context.md) for the per-route checklist and verification status.

## Bot API 10.4 sendWidget transport correction (2026-11-10)

- `sendWidget` must carry `allow_queries=true` for interactive widgets, not a hardcoded `false`, and send the initial `chat_data` for all state-driven widgets; static read-only detail widgets use `false`. Omitting either flag breaks UI behavior despite correct renderers and server handlers. This is enforced by `TelegramWidgetRequestMapperTest`.
- `chat_data`, `answerWidgetQuery.data`, and `editMessageWidgetData.chat_data` remain bounded to 1024 UTF-8 bytes; NUL characters are invalid in widget HTML/state. `TelegramWidgetMessage` now validates them at construction.
- No production rollout until Telegram 10.4 real-client tests demonstrate initial hydration, permitted requests, acknowledgement timing, and state updates.

## Account-management widget slice (2026-11-10)

- `/manage` and Account widget's `w1:acct:manage` now open an opt-in read-only `ACCOUNT_MANAGEMENT` widget. Snapshot and backend policy are owned by `AccountManagementGateway`; the bot checks the active account against the message-bound session before returning data or changing pages.
- Shared paging limits member/role widget state to Telegram's 1024-byte cap. Role assignment/revocation, confirmed kick, reply-bound role creation and confirmed role deletion now have typed application use cases and Redis replay guards. Account-service still owns all business permissions and hierarchy enforcement. Name/color editing, fine-grained permissions and hierarchy editing still use the legacy menu.
- The current implementation should not be called full account-management parity. See [working context](telegram-widget-feature-context.md).

## Account-management mutations and security (2026-11-10)

- `ACCOUNT_MANAGEMENT` is a feature-specific widget with account-bound Redis sessions, compact page-local indices and fresh authorized snapshots for every action. No client-supplied member/role UUID is trusted.
- `w1:manage:role:<page>:<member-index>:<role-index>:<on|off>` uses `ASSIGN_MEMBER_ROLES` and the existing `AccountManagementGateway.setMemberRole`. Non-owner self/owner role changes are disabled; owners retain the domain-approved owner exception. Account-service remains the final permission authority.
- Kick and role deletion require separate three-minute Redis confirmations bound to user, bot, widget message, current account and actual target ID. Query claims reject duplicate delivery of each mutation.
- `w1:manage:create` starts a ForceReply prompt for a role name; only a matching user/account/message reply can call the typed `createRole` use case. Input claim is at most once per prompt.
- A per-query Redis key is not domain-level exactly-once execution. Domain-level idempotency and an expected-active-account/version precondition remain necessary to protect against timeout ambiguity and cross-account races. Financial mutations must not rely on current guards.
- Tests were committed, but JVM builds and Telegram client tests were not executed. Leave widgets disabled for production.

## Role editor migration and stale-index safeguards (2026-11-10)

- The previously read-only management widget now supports existing `AccountManagementGateway.updateRoleDetails`, `setRolePermission` and `moveRole` operations through typed application use cases. Authoritative permission, hierarchy, base-role and actor-role policies stay in account-service.
- Editing role name/color uses a selected server-owned role ID stored in a versioned v2 private ForceReply input session; v1 sessions remain compatible for old input purposes. A role-edit reply requires matching prompt, widget, active account, role visibility and current permissions.
- Permission toggles pass only an enum index and desired state. The role's allowed permissions are encoded in a compact bitmask; the UI labels follow the documented `TelegramAccountManagementPermission` enum ordering.
- Hierarchy edits require `MANAGE_ROLE_HIERARCHY` and compare current rank against the originally rendered rank before invoking the gateway.
- All member/role writes now validate a short stable identity tag alongside the original page-local index. The identity tag is for stale-list detection and is not a bearer credential. No privileged command relies on client-provided IDs without a refreshed authorized snapshot.
- Remaining risks: account-switch race without expected-account precondition at the backend and nontransactional per-query replay claims. Do not declare full parity or enable widgets by default before builds, tests and real Telegram-client verification.

## Owner role assignment exception (2026-11-10)

- The widget mirrors `RoleAccessGuard` behavior: owners can manage their own role assignments, while non-owners cannot change their own or the owner's assignments. All operations still go through the domain gateway.
- Self-kick and owner removal remain prohibited. Changing permissions on roles assigned to the actor is additionally restricted by `RolePermissionMutationGuard`.
