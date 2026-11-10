# Mnemos Telegram Widgets — Feature Working Context

Last reviewed: 2026-11-10  
Tracking: [PR #274](https://github.com/knliiss/Mnemos/pull/274), branch `feat/telegram-widget-foundation`  
Canonical architecture decision: [ADR](adr-telegram-widget-foundation.md)

> **Read this before changing this feature.** This is a living handoff, not a claim that all work below has been deployed or verified. Update the implementation ledger and the next-step section in the same PR as each completed slice.

## Product goal

Expose **all feasible existing Mini App capabilities in the Telegram bot** with a cohesive hybrid interface. Telegram Bot API 10.4 **widgets are the primary interaction surface**; private-chat messages may collect user input and confirmations; the existing Mini App remains for interactions that are technically impractical or unsafe inside widgets, such as complex editors and files. Preserve the existing plain-text bot for clients without widget support. Feature parity requires an explicit per-capability audit, not a claim based only on the number of widget screens.

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
- `/events`: feed and filtering, pagination; search remains in the text command.
- `/account`: paged account selection; more complex account flows stay in chat.
- `/daily`: reward claim and streak restore through existing gateway.
- `/bets`: read-only odds, races, history; money-changing operations remain in existing chat flow.
- `/wallet`: balances and read-only, per-account authorized transaction history.
- `/online`: read-only realm/player overview; advanced search via existing command.
- Telegram widget-aware JSON polling shares the legacy update handling path while feature-enabled. Redis holds message-bound widget sessions; each widget slice has a query, use case, publisher port and renderer.
- Latest fix before this context: commit `7e84ef5` treats Telegram's “message is not modified” response to `editMessageWidgetData` as an idempotent success.
- Reply-bound chat input foundation: WidgetQuery acknowledges quickly, then Telegram `sendMessage` with `ForceReply` prompts the user; a separate 10-minute Redis key stores prompt ID, widget ID, owner, purpose and account context. Only a private user's reply to that specific prompt can be interpreted as a widget input; regular text and existing menu pending input retain their path.
- Read-only event search and online-player search reuse their **existing legacy menu search** contracts after collecting the query. Search results currently open in the text menu, not inside the original widget. This is deliberately an incremental bridge, not full in-widget parity.
- Known remaining concern: after a successful query acknowledgment, a failed prompt is not retried automatically. A new tap can request another prompt; the widget now explains this recovery and the service logs prompt failures.
- Last verified here: source inspection and new tests committed. Build/test/device compatibility have not been executed in this work session.

## Mini App route inventory for parity planning

Source of truth: `apps/mini-app/src/ui/navigation/routeRegistry.ts` (checked 2026-11-10). A route is **not** proof of a complete feature, and these are **widget coverage indicators**, not assertions that legacy chat lacks the capability. Audit the individual route's actions, roles, inputs, backend endpoints and error states before implementing parity.

| Mini App area | Route IDs | Current widget coverage / remaining work |
| --- | --- | --- |
| Events | `bets`, `events`, `event-detail`, `subscriptions` | Widgets cover read-only bets, event feed, simple subscription listing/ping/deletion; detailed event and complete rule editing not ported |
| Account | `account`, `leaderboards`, `guides`, `member`, `roles`, `role-editor`, `account-wallet`, `account-invites`, `account-settings`, `account-create`, `account-join` | Account selection and read-only wallet are partial; leadership, guide, member/role/invite/settings flows require a capability-by-capability audit |
| People | `search`, `public-profile`, `profile-activity`, `sao-realms`, `user-personalization`, `user-wallet` | SAO Online overview widget and chat-based nickname search are partial; profiles, activity and user-specific screens remain to map |
| Personalization | `personalization-store`, `personalization-store-category`, `personalization-owned`, `personalization-detail` | Not represented by dedicated widgets; purchase/ownership operations need typed permission and payment review |
| Creator | `creator-studio`, `creator-revenue`, `personalization-editor` | Not ported; retain Mini App for complex editor work, audit studio and revenue display separately |
| Staff | `moderation`, `administration`, `moderation-users`, `moderation-accounts`, `moderation-user`, `moderation-account`, `personalization-review`, `bet-admin`, `badge-admin`, `collector-admin`, `bot-agent-admin`, `wipe-admin` | No widget parity demonstrated; privileged operations must use explicit role checks and typed application commands |

Prioritize user-facing gaps by workflow, not by counting routes. For every item record `implemented`, `partially implemented`, `chat input`, `Mini App retained` or `unverified`, plus tests and security constraints. Do not invent parity from a similarly named screen.

## Next implementation priorities

1. **Widget-originated chat input:** initial read-only event / online reply-bound search in place with cancellation, TTL and a separate session. Next: user-visible prompt failure recovery, non-read-only operation confirmation, account-sensitive revalidation and direct widget result presentation. Never intercept unrelated text.
2. **Reliable query execution:** fast acknowledgement (<15 seconds), safe handling of retries and duplicate updates; avoid replaying a non-idempotent operation when a reply fails.
3. **State consistency:** per-message widget versioning, refresh coalescing and explicit handling of out-of-order update deliveries. Do not sync cross-widget state blindly.
4. **Coverage audit:** list every Mini App capability and map it to implemented widget, chat, retained Mini App, or gap with security/technical rationale. Extend profiles, personalization and administration one typed slice at a time.
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
