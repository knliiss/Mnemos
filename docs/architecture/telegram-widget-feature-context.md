# Mnemos Telegram Widgets — Feature Working Context

Last reviewed: 2026-11-10  
Tracking: [PR #274](https://github.com/knliiss/Mnemos/pull/274), branch `feat/telegram-widget-foundation`  
Canonical architecture decision: [ADR](adr-telegram-widget-foundation.md)

> **Read this before changing this feature.** This is a living handoff, not a claim that all work below has been deployed or verified. Update the implementation ledger and the next-step section in the same PR as each completed slice.

## Product goal

Transfer **every Mini App capability** to a coherent Telegram-first experience, including secondary screens, data mutations, settings, finance, moderation and creator operations. Telegram widgets must implement the interaction wherever technically supported; for unsupported inputs use a tightly correlated private-chat workflow. Only explicitly documented Telegram client limitations may justify retaining a complex operation in Mini App. Do not declare complete parity until every capability passes its acceptance tests. Telegram Bot API 10.4 **widgets are the primary interaction surface**; private-chat messages may collect user input and confirmations; the existing Mini App remains for interactions that are technically impractical or unsafe inside widgets, such as complex editors and files. Preserve the existing plain-text bot for clients without widget support. Feature parity requires an explicit per-capability audit, not a claim based only on the number of widget screens.

## Agreed decisions and invariants

1. Preserve Mnemos' existing Kotlin/Spring microservices, React Mini App, service ownership, internal authentication and bounded contexts. **No rewrite, new widget microservice, database cross-access or copied Mini App business logic.**
2. Use a modular hexagonal architecture within `telegram-bot-service`: inbound transport adapters → typed application use cases and ports → outbound Telegram/Redis/domain-service adapters. Pure UI renderers are presentation details. Keep one primary Kotlin class/type per file.
3. Continue incremental **vertical feature slices** within the agreed folder layout; do not silently reorganize packages or modules. Discuss nontrivial restructuring before applying it.
4. Share existing typed domain service operations and authorization rules across widgets, chat and Mini App. Enforce permissions, current user/account context, repeat-update safety and business idempotency at the corresponding operation boundary.
5. A widget query is accepted only for its private-chat owner with a valid message-bound Redis session. Reject shared/inline interactions, unknown actions and mismatched active-account context. Redis session keys are namespaced by bot, user and message, with TTL and versioning.
6. Keep existing text menus/commands and their Redis sessions working. When a widget is unsupported or rejected, preserve the text fallback; never run two Telegram `getUpdates` consumers simultaneously.
7. Keep client-facing widget payloads small: self-contained HTML bounded by Telegram constraints, bounded `chat_data` / `answerWidgetQuery` bytes, UTF-8 safe display values. Never forward arbitrary Telegram-supplied URLs, paths or transaction amounts to internal services.
8. Finance/privileged actions must remain in established, explicitly authorized, idempotent flows until a dedicated audited widget flow exists. Bets and wallet widgets are read-only in this increment.
9. Leave `mnemos.telegram-bot.widgets.enabled=false` by default. No production rollout or enabling the flag without real Telegram client validation, boundary tests and a rollback/kill switch.
10. User requested: continue implementation without focusing on CI for now. Still add targeted tests for modifications and report clearly what was or was not executed.

## Current implementation evidence (draft PR; not a production-parity claim)

- `/widget`: dashboard, navigation, account/balance context, daily claim.
- `/notifications`: ping setting, paged item-drop subscriptions, deletion; complex creation/editing uses legacy chat menu.
- `/events`: event feed, filtering, search and pagination in widgets; individual event detail now opens a read-only widget using a typed internal event-service endpoint. Following similar events, spotlight purchases and complete advanced filters remain unfinished.
- `/account`: paged account selection plus widget-originated create/join via reply-bound chat prompts; member roles, invites, account settings and privileged management still need dedicated widget surfaces.
- `/daily`: reward claim and streak restore through existing gateway.
- `/bets`: read-only odds, races, history; money-changing operations remain in existing chat flow.
- `/wallet`: balances and read-only, per-account authorized transaction history.
- `/online`: live realm overview, player search and refresh in widgets; full searchable lists need audit.
- Telegram widget-aware JSON polling shares the legacy update handling path while feature-enabled. Redis holds message-bound widget sessions; each widget slice has a query, use case, publisher port and renderer.
- Latest fix before this context: commit `7e84ef5` treats Telegram's “message is not modified” response to `editMessageWidgetData` as an idempotent success.
- Reply-bound chat input foundation: WidgetQuery acknowledges quickly, then Telegram `sendMessage` with `ForceReply` prompts the user; a separate 10-minute Redis key stores prompt ID, widget ID, owner, purpose and account context. Only a private user's reply to that specific prompt can be interpreted as a widget input; regular text and existing menu pending input retain their path.
- Event search and SAO Online player search now **edit their original widgets** with the search result and persist search context in versioned Redis sessions. The underlying domain queries are reused and SAO nickname matching is shared with the legacy menu. Invalid input still uses the legacy menu feedback as a fallback.
- Widget-originated account create/join now has one-time Redis mutation claims per reply prompt, active-account context rechecks, typed AccountGateway delegation and widget refresh feedback. The claim prevents replay of the same reply, but not duplicate user actions across different prompts; domain-level idempotency and an auditable result record remain necessary.
- Known remaining concern: after a successful query acknowledgment, a failed prompt is not retried automatically. A new tap can request another prompt; the widget now explains this recovery and the service logs prompt failures.
- On temporary failure after a mutation claim, the reply is consumed rather than automatically replayed; users must inspect their account list before retrying a create/join. Do not extend this pattern to monetary operations without domain-side idempotency.
- Last verified here: source inspection and new tests committed. Build/test/device compatibility have not been executed in this work session.

## Mini App route inventory for parity planning

Source of truth: `apps/mini-app/src/ui/navigation/routeRegistry.ts` (checked 2026-11-10). A route is **not** proof of a complete feature, and these are **widget coverage indicators**, not assertions that legacy chat lacks the capability. Audit the individual route's actions, roles, inputs, backend endpoints and error states before implementing parity.

| Mini App area | Route IDs | Current widget coverage / remaining work |
| --- | --- | --- |
| Events | `bets`, `events`, `event-detail`, `subscriptions` | Read-only bets, event feed, event detail, item-drop/booster/dungeon/global/raid subscription widgets are present; following events, presence alerts, advanced rule editing and money-changing actions remain |
| Account | `account`, `leaderboards`, `guides`, `member`, `roles`, `role-editor`, `account-wallet`, `account-invites`, `account-settings`, `account-create`, `account-join` | Account selector, create/join, invitation listing/create/revoke, and read-only wallet have widget slices; leaderboards, guides, member/roles and account settings still absent |
| People | `search`, `public-profile`, `profile-activity`, `sao-realms`, `user-personalization`, `user-wallet` | SAO Online, authenticated public-profile directory search/details and profile drop activity have widget slices; comments/voting, full activity, user settings/wallet remain incomplete |
| Personalization | `personalization-store`, `personalization-store-category`, `personalization-owned`, `personalization-detail` | Not represented by dedicated widgets; purchase/ownership operations need typed permission and payment review |
| Creator | `creator-studio`, `creator-revenue`, `personalization-editor` | Not ported; retain Mini App for complex editor work, audit studio and revenue display separately |
| Staff | `moderation`, `administration`, `moderation-users`, `moderation-accounts`, `moderation-user`, `moderation-account`, `personalization-review`, `bet-admin`, `badge-admin`, `collector-admin`, `bot-agent-admin`, `wipe-admin` | No widget parity demonstrated; privileged operations must use explicit role checks and typed application commands |

Prioritize user-facing gaps by workflow, not by counting routes. For every item record `implemented`, `partially implemented`, `chat input`, `Mini App retained` or `unverified`, plus tests and security constraints. Do not invent parity from a similarly named screen.

## Next implementation priorities

1. **Widget-originated chat input:** event and online searches update their own widgets; account create/join use prompt-scoped at-most-once claims. Next: durable operation receipts, domain idempotency, user-visible failure recovery and additional typed inputs (subscriptions, invitations, roles). Never intercept unrelated text.
2. **Reliable query execution:** fast acknowledgement (<15 seconds), durable operation receipts and backend idempotency. Widget invitation create uses Redis one-query claim, and revoke requires a three-minute message-bound confirmation; those do not guarantee exactly-once business execution during backend timeouts.
3. **State consistency:** per-message widget versioning, refresh coalescing and explicit handling of out-of-order update deliveries. Do not sync cross-widget state blindly. Keep invite confirmation and mutation claims separate from widget page/search sessions.
4. **Coverage audit:** extend profile social workflows using explicit Telegram-user-to-account actor authentication in social-service, not a service-key impersonation. Then finish event-detail follow actions, account member/roles and user-visible subscriptions before cosmetics or creator tools.
5. **Verification:** unit/adapter tests, integration and real-client tests, widget sandbox inspection, staged opt-in/canary before enabling.

## Working protocol for future sessions

- Read this file **and the ADR**, the current PR diff/head and actual source before adding code; prior summaries are not proof that a feature works.
- State what is confirmed, what is proposed, and what was actually tested.
- Keep work on `feat/telegram-widget-foundation` / PR #274 until instructed otherwise; do not modify `master` or deploy.
- For each slice: implement application contract → inbound adapter → outbound adapter → tests, then update this file with a precise implementation note, any new agreed decision and next unresolved dependency.
- Never record access tokens, chat/user identifiers, personal data or secrets here.

## Implementation ledger

| Date | Change | Validation |
| --- | --- | --- |
| 2026-11-09 | Foundation widget slices and architecture ADR added to draft PR #274 | Source and tests committed; real device/CI status not confirmed |
| 2026-11-10 | Idempotent unchanged-state Telegram widget edit handling (`7e84ef5`) | Commit verified; runtime not executed |
| 2026-11-10 | Created this living feature context and continuation checklist | File committed in the feature branch |
| 2026-11-10 | Added reply-bound ForceReply input for event search; event widget can start search (`56e6558`) | Source reviewed; client/CI unverified |
| 2026-11-10 | Reused pending input for SAO Online player search; added use-case/inbound tests | Tests committed; execution still pending |
| 2026-11-10 | Added mini-app route inventory from routeRegistry; hardened prompt failure behavior and tests | Static source review; no CI/device test run |
| 2026-11-10 | Shared SAO matcher, versioned `v4` widget search sessions, direct search result updates for events and SAO Online | Source and tests committed; builds/devices unverified |
| 2026-11-10 | Widget-originated account creation and joining through reply-bound chat with one-time Redis mutation claims | Application/adapter tests committed; builds/devices unverified |
| 2026-11-10 | Settings widget for language and notification-pack selection, dashboard navigation and legacy fallback | Static action matching completed; tests committed but not executed |
| 2026-11-10 | Added invitation widget with list/create/revoke, 3-minute confirmation and Redis replay guard | Unit tests committed; not executed |
| 2026-11-10 | Added widget control of booster/dungeon/global/raid and reply-bound item-drop creation | Unit tests committed; not executed |
| 2026-11-10 | Linked public profile to paged player drop activity; added exact authorized internal event-service `droppedFor` filter and new activity widget | Unit tests committed; build/client execution not verified |
| 2026-11-10 | Added read-only event-detail widget using authorized `event-service` ID lookup and server-bound event selection from current feed | Unit tests committed; following and spotlight actions remain unavailable |
| 2026-11-10 | Fixed initial widget data and the `allow_queries` transport flag, plus NUL validation and request mapper tests (`3b66767`) | Static inspection and unit tests added; client/build not run |


| 2026-11-10 | Privacy-aware internal public profile API, profile-search/detail widget, and drop-activity widget | Source and tests committed; compilation and client tests not run |

## Per-route migration checklist (not a completed-parity statement)

These are *route-level* statuses derived from Mini App routing and selected UI source. Each route can contain multiple additional actions that still require individual review. **Partial** means the route has some widget coverage, not that all actions are equivalent.

| Mini App route | Current status | Remaining widget parity work |
| --- | --- | --- |
| `bets` | Partial | Wager placement, increase, payout view, history and full race/event bet interactions |
| `events` | Partial | Event type-specific filters, sort, wipe/period selection and complete pagination |
| `event-detail` | Partial | Read-only event details for item drops, boosters, globals and raids now open from the widget feed. Follow/unfollow, related feed links, own-account highlighting and spotlight purchase remain |
| `subscriptions` | Partial | Widget can now create item-drop rules via reply-bound input, toggle booster/dungeon/global subscriptions, configure 1–28 raid locations and list/delete drops; player-offline alerts, per-rule editing, audit and advanced controls remain |
| `account` | Partial | Full account dashboard, members and navigation to all subfeatures |
| `leaderboards` | Missing | Drops/coins/boosters tabs, periods, rankings and pagination |
| `guides` | Missing | Permission-gated guides and guide reader |
| `member` | Missing | Member detail and permitted member operations |
| `roles` | Missing | Roles overview, rank, permissions |
| `role-editor` | Missing | Add/edit roles, hierarchy and permission editor with confirmations |
| `account-wallet` | Partial | Account wallet transactions present; full Mini App wallet action mapping outstanding |
| `account-invites` | Partial | Widget listing, creation with expiry options, confirmation-backed revocation and joining by code now present; invite sharing, user notification and audit/retry verification outstanding |
| `account-settings` | Missing | Account configuration and permission-gated settings |
| `account-create` | Partial | Reply-bound creation available; onboarding polish and full error recovery missing |
| `account-join` | Partial | Reply-bound join available; onboarding polish and full error recovery missing |
| `search` | Partial | Public profile directory search and SAO nickname search are available in widgets; broaden filter/refresh parity and verify Telegram client behavior |
| `public-profile` | Partial | Basic profile, reaction counters and privacy-aware presence available; comment feed/actions and personalization visuals remain |
| `profile-activity` | Partial | Public item-drop activity and cursor-based navigation present; other activity categories and full Mini App parity remain |
| `sao-realms` | Partial | Live realm/player views exist; full lists and complete navigation missing |
| `user-personalization` | Missing | User personalization configuration |
| `user-wallet` | Missing | User PRIME wallet distinct from account wallet |
| `personalization-store` | Missing | Browse listings, filtering, categorization |
| `personalization-store-category` | Missing | Category-specific catalog and filtering |
| `personalization-owned` | Missing | Owned item browsing and applying |
| `personalization-detail` | Missing | Item previews, purchasing, entitlement and applying |
| `creator-studio` | Missing | Creator assets, status and management actions |
| `creator-revenue` | Missing | Creator earnings and transaction history |
| `personalization-editor` | Mini App retained provisionally | Complex visual editor; assess widget support before final exception approval |
| `moderation` | Missing | Moderator dashboard and navigation |
| `administration` | Missing | Admin overview and role-gated navigation |
| `moderation-users` | Missing | User search, filters, pagination, sanctions |
| `moderation-accounts` | Missing | Account moderation list and filtering |
| `moderation-user` | Missing | User review and moderation actions |
| `moderation-account` | Missing | Account review and moderation actions |
| `personalization-review` | Missing | Review queue and approval/rejection commands |
| `bet-admin` | Missing | Betting controls and audit workflows |
| `badge-admin` | Missing | Badge listing and management |
| `collector-admin` | Missing | Collector monitoring and privileged commands |
| `bot-agent-admin` | Missing | Bot/agent administration with allowlisted commands |
| `wipe-admin` | Missing | Wipe management, scheduling and protections |

**Acceptance rule:** a route is complete only after its own Mini App actions, permissions, error states, data visibility, refresh behavior, and fallback behavior have been audited and matched against a widget/chat implementation with tests.

## 2026-11-10 continuation: widget-native search, account operations, and settings

- **Search:** the originating Events/SAO Online widget is updated with matching results; searches are scoped to its versioned message-bound Redis session. The SAO nickname match policy is shared by both legacy menu and widget. Reset/refresh has explicit UI actions.
- **Account onboarding:** the Accounts widget can initiate account creation and joining through a ForceReply message, using existing `AccountGateway`. A Redis prompt claim prevents processing a particular input twice. This is *not* equivalent to durable domain-level idempotency across different prompts or retries after indeterminate downstream failure. Account list is refreshed after the operation.
- **Settings:** `/settings` opens a ninth widget screen (with legacy fallback), using the existing `SettingsGateway` to read and change `UK/RU/EN` language and choose from visible authorized notification packs. Dashboard navigation and old-menu fallback are wired.
- **Tests:** application tests were added for the above scenarios and Redis session encoding / mutation claims; not run in this session. Telegram 10.4 device testing remains outstanding. Static inspection found matching action strings for nine current widget renderers and handlers; this is not a runtime validation.
- **Remaining parity:** public profiles, leaderboards, detailed events, every subscription type, invitations, members/roles, marketplace/purchases/equip flows, creator tools, admin/moderation and finance workflows still need route-by-route implementations and validation. See the per-route checklist. The final feature is **not** complete.

### Next implementation increments

1. Implement public-profile search and profile detail (including permissions, comments/ratings, and activity) with typed service contracts; avoid querying other services' databases.
2. Implement account invitations, member/role management, and all notification subscription types with explicit user confirmation and domain-backed idempotency for mutations.
3. Implement store browsing/owned personalization and safe financial flows; user-wallet and account-wallet access must remain strictly separated and authorized.
4. Implement privileged creator and administration workflows with typed commands, server-side authorization, auditability, and guardrails; retain the graphical editor only if widget/client limitations are verified.
5. Validate all Kotlin compilation, tests, Telegram Sandbox and real Telegram clients. Keep `mnemos.telegram-bot.widgets.enabled=false` until verified.

## Continuation checkpoint — 2026-11-10 (invites and subscriptions)

### Implemented (code on PR, not deployed or verified in a real client)

- **Account invitations:** new `/invites` widget and Accounts-widget navigation; lists 3 invites per page, shows exact codes and validity/status, creates invites with `NONE / ONE_HOUR / ONE_DAY / SEVEN_DAYS`, revokes only visible active invitations after message-bound confirmation. The existing `AccountInviteGateway` still decides permission and active-account eligibility.
- Invitation mutation safety: Redis SHA-256-scoped query claim (1 day) prevents the *same WidgetQuery ID* from executing twice; revocation confirmation is consumed atomically and expires in 3 minutes. Neither mechanism is a durable business transaction receipt, nor does it deduplicate two distinct intentional clicks. There is still a potential indeterminate-state problem if an internal service times out after committing a mutation.
- **Core subscription modes:** widget toggles `BOOSTER`, `DUNGEON`, `GLOBAL` selections with desired end-state commands, and can configure all 28 raid locations through a validated 28-bit base36 mask. Reuses typed subscription gateway, existing enum validators and `NotificationRaidLocations` conventions; existing ownership remains service-enforced.
- **Drop subscriptions:** widget starts reply-bound chat prompts for a player, an item, or both in `nickname;item` form. Reuses `NotificationItemDropInputValidator` and `NotificationSubscriptionCreationGateway`; a Redis prompt claim prevents replay of that specific input; after creation the originating widget is refreshed with feedback.
- New source and adapter tests for authorization, confirmation, replay, parsing, toggling and snapshot mapping are committed, **but not executed**. No source changes have been deployed.
- **Remaining in core notifications:** player-presence/offline subscription flows, richer drop-rule editing, cross-widget synchronization, durable business-level idempotency, actual Telegram client/sandbox and integration tests.
- **Profiles security:** `profile-service` exposes `/api/v1/profiles` behind JWT while Telegram bot's service-to-service authentication uses `/internal/**` API keys. Do **not** bypass this by calling JWT endpoints without a user token. Any profile widget requires an explicit authenticated internal read contract, correctly provisioned service API keys, and privacy-aware mapping of hidden presence.

## Continuation checkpoint — 2026-11-10 (public profiles)

**Implemented in PR, but neither deployed nor runtime-verified:**

- New `contracts/profile-contracts` internal DTOs for a deliberately limited public-profile read model. `profile-service` exposes `GET /internal/v1/telegram/profiles?q=...&page=...&size=...` and `GET /internal/v1/telegram/profiles/{accountId}`, reusing existing public profile search/get use cases. Existing JWT-protected APIs are untouched.
- Internal routes use established service-key authentication. The Telegram service uses a typed Feign adapter with `X-Mnemos-Service` and `X-Mnemos-API-Key`. The deployment keys `PROFILE_SERVICE_TELEGRAM_BOT_API_KEY` and `TELEGRAM_BOT_SERVICE_PROFILE_API_KEY` **must match**; only templates and compose wiring were updated.
- The profile projection contains public account identity, username, social counters, presence and join date. If presence is hidden, the profile-service mapper strips realm and last-seen, and the Telegram client adapter masks those fields again defensively.
- `/profiles` or `/profile` plus a dashboard button opens the public profile widget. Reply-bound search reuses the profile gateway; widget pages open visible public profile details. `v5` Redis sessions store the selected profile ID, with backward reads for `v2/v3/v4`.
- A separate public profile activity widget shows available item-drop history with cursor navigation, reusing event history queries. It is **not** the full Mini App activity screen.
- Targeted tests have been committed for mapping, privacy boundaries, widget rendering, input and navigation, and old session compatibility. **No test was executed** in this session.

**Remaining:** comments/likes/dislikes actions through social-service with proper authorization; detailed profile personalization, all activity types, full user wallet, leaderboards, account roles, creator and administrative actions, domain-level idempotency and integration/client testing. Do not enable widgets in production until these checks pass.

### Architecture decision

Do not reuse user JWTs inside service-to-service calls or query other services' databases. Profile read contracts remain separate from social mutations; future writes require explicitly permission-checked, idempotent use cases rather than generic transport proxies.

## Follow-up checkpoint — profile activity and event details (2026-11-10)

- **Profile activity** is a separate read-only widget opened from a selected public profile. It queries `EventHistoryGateway` with exact `droppedFor` and enforces server-side cursor validation; the underlying `event-service` endpoint is restricted to the authenticated `telegram-bot-service` internal service principal. The new `droppedFor` parameter is optional for existing event callers.
- **Event details** are read-only widgets opened only for an item belonging to the current server-loaded event page. A dedicated `event-service` internal endpoint returns the existing event projection by ID; the Telegram feature uses a typed gateway, with no browser-originated event ID accepted directly. Detail widgets show type-specific facts but not follow/unfollow or spotlight purchase actions yet.
- **Social reactions/comments** cannot be ported by treating a service API key as an end-user identity. The current social use cases rely on `SocialActorProvider` and `CurrentMnemosUser`, plus account permission checks. Before exposing writes from Telegram widgets, add a server-authorized actor resolution flow mapping Telegram identity to an authenticated Mnemos user and active account, preserving permissions, audit, and idempotency.
- **Validation gap**: tests added for activity pagination, exact drop filtering, shortcut behavior, bound event selection and detail mapping. No Gradle build or Telegram client test was run in this session; the feature flag must remain off by default.
- **Immediate next priorities**: (1) actor-aware social/profile comments and reactions, (2) event detail follow/unfollow, (3) account members/roles, (4) full notification presence rules, (5) finances and personalization once domain-side idempotency is proven.

## Transport correctness audit — Telegram Bot API 10.4 (2026-11-10)

- **Critical foundation fix**: the pre-existing `TelegramWidgetRequestMapper` had been sending `allow_queries=false` for *all* widgets and omitted initial `chat_data`. This made embedded WidgetQuery actions unavailable and left dynamically rendered screens without their initial state. Its payload now respects `TelegramWidgetMessage.allowQueries` and includes the initial `chat_data` when supplied. Empty inline keyboards are omitted. The read-only event-detail widget deliberately keeps queries disabled while retaining its initial state.
- Validate against official documentation: https://core.telegram.org/bots/api#sendwidget and https://core.telegram.org/bots/widgets. Added mapper tests for both interactive and read-only widgets and rejects NUL characters in HTML/chat data. Previous tests incorrectly asserted that all widget queries were disabled; that assertion was updated.
- **Verification still missing**: no local Gradle build, CI run, actual Telegram bot delivery, or desktop/mobile Telegram-client test has been executed in this session. This is a committed code fix, not a live verification.
- **Known delivery risks**: 1024-byte query replies and widget state, 15-second acknowledgement deadlines, in-flight duplicate queries and out-of-order state edits still require integration and real-client verification. Keep the feature flag disabled in production.
