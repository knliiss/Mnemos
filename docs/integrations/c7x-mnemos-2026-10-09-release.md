# C7xBotManager × Mnemos — release checklist (2026-10-09)

Status: **candidate, NOT authorized for production until all gates pass**. The C7x application remains running on separate Windows PCs; Mnemos receives only outbound WSS connections. This release introduces **no cloud Minecraft runtime**.

## Scope

- Mnemos realtime-service: bot-agent WSS endpoint, heartbeat/session management, REST RPC proxy and admin controls, persistent device registry and account groups.
- Mnemos gateway-service: public **handshake only** at `/ws/v1/bot-agent`; admin REST requests continue to require the ADMIN/SUPER_ADMIN role.
- Mnemos mini-app: illustrated C7x worker administration, account/group controls, automation, proxy pools, spoof options, chat and logs.
- C7xBotManager desktop/backend: local Mnemos pairing screen, DPAPI-encrypted secret, outbound WebSocket client, local API allowlist.

## Merge / delivery order

1. **Gate 1 — C7x checks:** backend Java tests, Windows installDist / portable build, `node --check web/app.js`, `python scripts/verify-web-ui.py`. Validate bundled JNA/JRE and Windows DPAPI credential storage. Do not publish a desktop release until these pass.
2. **Gate 2 — Mnemos checks:** backend compilation and tests for realtime-service/gateway-service, mini-app `npm ci && npm run verify` (including formatting, lint, tests, TS build), integration tests. Full stack smoke only when interface/infrastructure changes require it, not for every one-file patch.
3. Review `knliiss/Mnemos#236` and `knliiss/C7xBotManager#6` as a coordinated change. Back up PostgreSQL prior to schema deployment and retain existing service image tags/Cloudflare Pages deployment IDs for rollback.
4. Deploy **realtime-service and gateway-service** together, confirm Flyway V4 and V5 are applied to the `realtime` schema; verify both services healthy and `/ws/v1/bot-agent` routed correctly. Do not put the worker key in application logs or frontend environment variables.
5. Deploy the **Mnemos mini-app via its existing, protected Cloudflare Pages production workflow** from the reviewed master commit. Do not use the removed preview workflow.
6. Distribute the verified Windows **C7xBotManager** build to one canary PC, pair through its local Mnemos dialog with a key created in the admin screen, then gradually enable additional PCs.

## Canary acceptance

- Without a key, the worker WebSocket handshake is rejected. With a valid key, C7x receives `ready` and appears online in Mnemos with OS username and stable machine fingerprint.
- A connected worker replies to `GET /api/accounts`, reports basic status, and permits one safe test operation through the administrator UI.
- Test single-account commands (connect, disconnect, SAO, navigation) and mass commands **only on explicitly designated test accounts**. Verify real account login/OTP, dungeon toggles, realm selection, chat, proxy selection and log retrieval.
- Create and rename a group; assign an account; reload from another device; confirm persistence and revision conflict protection.
- Restart C7xBotManager: the same worker record returns online, with no new pairing key. Stop the PC: the record remains listed offline. Revoke a test worker key: its active session must close and reconnection must be rejected.
- Confirm non-admin Keycloak users cannot access worker-management REST endpoints. Confirm the local dangerous paths `/api/runtime/shutdown` and `/api/runtime/reload` are unreachable over the remote RPC.
- Inspect logs for unexpected token/password disclosure, failed heartbeat reconnect loops, extra duplicates, and increased CPU/network load on a densely populated C7x instance.
- Observe one canary under normal network interruption before expanding to all bots.

## Rollback

- Restore previous mini-app Cloudflare Pages deployment first if the UI regresses; revert gateway/realtime-service containers to the previous immutable images if transport fails.
- **Do not downgrade DB destructively:** V4/V5 are additive and can remain unused if service binaries are rolled back. Keep encrypted keys only on each PC; revoke canary keys through the admin endpoint if an access issue occurs.
- Disable Mnemos linking per PC by closing C7x or clearing its pairing only through a supported local operator workflow. Do not delete bot vaults or unrelated account state.

## Release blockers to resolve before pressing Deploy

- GitHub Actions have **not produced passing CI for these branches**: Mnemos jobs are queued because their self-hosted runners are unavailable; the C7x hosted jobs fail before steps begin. Neither is a verified build.
- **Resolved:** historical `C7x Interface Preview` run 37706528042 was cancelled, the superseding no-op run 37707210367 finished skipped, and the temporary preview workflow/demo HTML have been removed. No preview deployment is planned.
- Format/check the new mini-app files and verify successful Kotlin/Java compilation and integration against live gateway/realtime-service before marking both PRs ready.
