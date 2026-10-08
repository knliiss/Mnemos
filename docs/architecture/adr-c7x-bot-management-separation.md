# ADR — C7x remote bot management (target separation)

Status: **required before production; current PR #236 does NOT conform**.

## Boundary

The present implementation has `BotAgentRegistry`, `BotAgentSessions`,
`BotAgentAdminController`, and `BotAgentGroupsController` in
`apps/realtime-service/src/main/kotlin/com/mnemos/realtime/botagent/`.
This is an interim implementation only. **Do not deploy it as is.**

### Public request and command path

```text
Mnemos mini-app
  -> API gateway (JWT validation and admin authorization)
  -> bot-management-service (owner of bot operations and persistent state)
  -> authenticated internal command relay
  -> realtime-service (transport only, WebSocket session and response correlation)
  -> outbound WSS connection to C7xBotManager on the user's Windows PC
  -> local allowlisted C7x HTTP API
```

Only the authorized local C7x process performs Minecraft actions. Mnemos must
not start Minecraft clients or store their account credentials.

### New `bot-management-service` must own

- persistent worker identity, hashed pairing keys, rename/revoke and last-seen projections
- durable OS-username based device labels, account group definitions/assignments and revisions
- admin REST controllers and permission checks
- account/bulk command request validation and orchestration
- audit events for who initiated sensitive commands and their results
- worker management database schema and **dedicated least-privilege database role**
- async command timeout, concurrency and per-agent backpressure policies

### Existing `realtime-service` may own ONLY

- secure worker handshake to the management service (via internal authenticated API)
- WebSocket connection lifecycle, hello, heartbeat, ping/pong and session-local correlation
- command dispatch to a connected worker and response delivery through an authenticated
  transport mechanism; no user-facing bot admin REST endpoints
- resource limits for concurrent connections, requests and message sizes

It must not query a bot management database or contain group/account business logic.

### Transport and security

Internal management-to-transport requests must be authenticated, permission-isolated
from browser requests and bounded by timeouts. A separate dedicated secret/service
identity is mandatory: do not expose the internal transport command endpoint through
the public gateway; do not reuse raw pairing secrets for intra-service auth.

For multi-instance realtime, command dispatch must locate the specific instance that
holds the agent's WebSocket, using an explicit shared routing/relay mechanism (Redis
stream / broker or equivalent). A local ConcurrentHashMap alone is NOT a cross-replica
dispatch solution.

Worker pairing tokens are shown only once, stored hashed server-side and DPAPI-encrypted
on Windows. Revoke must close the active connection and prevent reconnection. Avoid
account passwords and sensitive RPC bodies in persistent logs.

### Database and deployment

1. Add Gradle app module, Docker image, own schema, DB role and Flyway migrations.
   Move the as-yet-undeployed bot-agent tables out of realtime's V4/V5 migrations.
2. Migrate registry/groups/admin orchestration to the new service; keep only the
   socket handler/transport interface in realtime.
3. Wire gateway REST to bot-management, WSS to realtime, and explicit internal
   authenticated routing between the two services.
4. Update compose, production service catalog, secrets bootstrapping, CI affected
   module detection and smoke/integration test suites.
5. Test across two realtime instances (or expressly constrain realtime to one
   instance until that functionality is implemented).
6. Canary on one C7x Windows worker before production-wide rollout.

### Non-negotiable release gate

PR #236 and C7x PR #6 stay Draft and MUST NOT merge or deploy before service
ownership separation, passing compile/lint/security checks and successful
end-to-end worker pair/reconnect/revoke tests.

*Reason:* isolate potentially high-churn bot control, protect ordinary Mnemos
realtime event delivery and allow the bot-management service to scale/fail
independently.
