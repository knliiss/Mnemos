# Player directory manual testing

This guide prepares `feat/player-directory-presence` for manual testing without merging or deploying the branch.

## Prerequisites

Install the tools used by the repository:

- Docker with Docker Compose;
- Java/Gradle through the checked-in `./gradlew` wrapper;
- Node.js exactly as declared in `apps/mini-app/.node-version`;
- npm;
- an HTTPS route for the Telegram Mini App frontend;
- public HTTPS routes for the local Mnemos API gateway and Keycloak if the Mini App is opened from Telegram;
- valid Telegram bot and Cristalix credentials.

The Mini App authentication flow intentionally requires `window.Telegram.WebApp`. Opening the Vite URL directly in a normal browser is useful for static development but is not a valid end-to-end authentication test.

## Environment

Create the root environment once if it does not exist:

```sh
cp .env.example .env
```

Replace every placeholder/blank required secret with a unique real value. For player-directory testing, the important external values are:

```text
MNEMOS_TELEGRAM_BOT_TOKEN
CRISTALIX_INTERACTIVE_PROJECT_TOKEN
CRISTALIX_INTERACTIVE_PROJECT_KEY
CRISTALIX_MONITORING_PROJECT_TOKEN
CRISTALIX_MONITORING_PROJECT_KEY
CRISTALIX_RESERVE_PROJECT_TOKEN
CRISTALIX_RESERVE_PROJECT_KEY
```

Set these three origins to the exact HTTPS endpoints used by the manual-test environment:

```text
MNEMOS_FRONTEND_ORIGIN
MNEMOS_API_ORIGIN
MNEMOS_AUTH_ORIGIN
```

`MNEMOS_FRONTEND_ORIGIN` must be the URL opened by Telegram. During local Vite testing it should point to an HTTPS tunnel/preview that forwards to `127.0.0.1:5173`.

`MNEMOS_API_ORIGIN` must route to the local gateway (`GATEWAY_HOST_PORT`, default `8080`). `MNEMOS_AUTH_ORIGIN` must route to local Keycloak (`KEYCLOAK_HOST_PORT`, default `8079`).

Do not use the `*.example.com` values from `.env.example`: `make test-ready` rejects them deliberately.

## Prepare everything

From the repository root:

```sh
make test-ready
```

The target performs the following operations in order:

1. validates all Compose secrets;
2. validates the three HTTPS origins;
3. checks the exact Node.js version;
4. generates `apps/mini-app/.env.local` from the root environment;
5. installs Mini App dependencies with `npm ci`;
6. runs the Mini App audit, lint, formatting check, tests and production build;
7. synchronizes Kafka message-auth keys;
8. runs the complete backend Gradle build;
9. builds Docker images;
10. recreates the stack;
11. validates container state and Kafka message authentication.

The generated `apps/mini-app/.env.local` is local-only and ignored by Git. It keeps Vite API/auth URLs aligned with the backend environment and allows only the hostname from `MNEMOS_FRONTEND_ORIGIN` through Vite's host protection.

After preparation, verify the stack explicitly:

```sh
make status
```

## Run the feature-branch Mini App

Start Vite from the repository root:

```sh
make mini-app-dev
```

The default listener is `127.0.0.1:5173`.

Expose that listener through the HTTPS frontend route configured as `MNEMOS_FRONTEND_ORIGIN`, then use that exact URL as the Telegram Mini App test URL. The repository intentionally does not deploy this feature branch to the production Cloudflare Pages project; production deployment remains master-only.

If the HTTPS frontend hostname changes, update `MNEMOS_FRONTEND_ORIGIN` and run `make test-env-prepare` again before restarting Vite. If API/auth origins change, rerun `make test-ready` because backend CORS/Keycloak configuration must stay aligned too.

## Manual test checklist

### 1. Startup and authentication

- `make status` reports every long-running service as running/healthy and permits successful one-shot services to remain exited with code 0.
- Open the Mini App from Telegram and authenticate normally.
- Reload the Mini App while authenticated; the session should resume without an unnecessary login loop.
- Leave the Mini App open long enough for token maintenance, then foreground it again; the session should refresh or recover cleanly.
- Temporarily make Keycloak unavailable and confirm the Mini App shows the service-unavailable state instead of crashing.

### 2. Player lookup and canonical identity

- Search for an existing player by current nickname.
- Open the same player by UUID-backed navigation/surfaces and confirm the same identity is shown.
- Verify skin/head rendering for the player.
- If a known renamed player is available, confirm the current nickname resolves to the UUID and historical nickname snapshots are not rewritten.

### 3. SAO realm directory

- Open the SAO realm directory.
- Confirm only realms matching `SAO-[1-9][0-9]*` appear.
- Confirm multiple/dynamic realms are shown instead of a fixed hard-coded realm count.
- Refresh and confirm the directory remains responsive while the backend cache is active.
- Compare displayed player heads/nicknames with player profile pages.

### 4. Presence

- Open an ONLINE player and confirm ONLINE state/realm display.
- Open an OFFLINE player and confirm OFFLINE state.
- Verify UNKNOWN/HIDDEN states are not presented as OFFLINE.
- Create a presence subscription for an ONLINE or OFFLINE player; it should persist immediately.
- Attempt to subscribe to an UNKNOWN/HIDDEN/unavailable player; it must fail and must not create a subscription.
- Remove a subscription and confirm it disappears from the list.

### 5. Offline notification

- Use a subscribed player whose state can be changed from ONLINE to confirmed OFFLINE.
- The first offline observation must not immediately create a false offline transition when confirmation requirements are not yet satisfied.
- After the confirmed transition, verify `PLAYER_OFFLINE` notification delivery.
- Confirm the notification uses the selected Notification Pack snapshot/art rather than a generic event fallback.

### 6. Event identity/search

- Find item-drop events by current player nickname.
- Confirm search semantics are current nickname -> current UUID -> UUID events.
- Search by item/drop criteria and confirm existing event sorting/filtering remains intact.
- Verify player heads on UUID-enriched drop/booster surfaces.
- A historical event should retain its historical nickname snapshot where that snapshot is part of the event record.

### 7. Leaderboards

- Open DROPS, COINS and BOOSTERS tops.
- Confirm player identity/head links resolve by UUID.
- Repeat the same view within five minutes and confirm behavior remains stable with cached leaderboard data.
- With admin access, add an ACCOUNT or PLAYER exclusion for a specific scope and confirm exclusion is applied before ranking/LIMIT.

### 8. Personalization / Notification Packs

- Open Notification Pack preview/detail surfaces.
- Confirm all six notification templates are represented, including `PLAYER_OFFLINE`.
- Open Creator Studio and confirm `PLAYER_OFFLINE` uses the dedicated offline art rather than eclipse/global-event art.
- Compare Store/detail/Creator preview rendering for the same pack.

### 9. Account/player reconciliation

- Use an older Account that existed before the player-directory event projection was introduced.
- Confirm background bootstrap eventually links its `accountId` to the matching Cristalix player UUID.
- Confirm SAO realm reads do not need a synchronous Cristalix HTTP call to obtain that account link.

### 10. Degradation checks

- Stop `player-directory-service` and confirm dependent UI surfaces fail cleanly rather than breaking the whole Mini App.
- Restart it and verify recovery.
- Do not run multiple player-directory replicas during this test: Cristalix quota limiting is still process-local and the current operational contract is a single scheduler replica.

## Useful commands

```sh
make test-env-prepare
make mini-app-install
make mini-app-verify
make test-ready
make mini-app-dev
make status
make logs
make player-directory-status
make player-directory-logs
make player-directory-restart
```

Use `make restart` after backend code/config changes. Use `make mini-app-verify` after frontend-only changes when the backend stack does not need to be recreated.
