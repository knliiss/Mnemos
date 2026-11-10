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
- Last verified here: source inspection of the branch and existing ADR. Build/test/device compatibility not verified in this work session.

## Next implementation priorities

1. **Widget-originated chat input:** correlate a private user's reply to a specific widget message and a typed pending operation. Handle cancellation, TTL, stale account context and conflict with existing menu input. Do not hijack unrelated user text. Reuse existing use cases for the final mutation.
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
