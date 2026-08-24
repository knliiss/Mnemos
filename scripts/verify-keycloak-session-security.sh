#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
REALM_CONFIG="$REPOSITORY_ROOT/infrastructure/keycloak/config/00-realm-settings.yaml"
CLIENT_CONFIG="$REPOSITORY_ROOT/infrastructure/keycloak/config/40-clients.yaml"
MINI_APP_AUTH="$REPOSITORY_ROOT/apps/mini-app/src/features/auth/api/auth.ts"

fail() {
    echo "$1" >&2
    exit 1
}

for file in "$REALM_CONFIG" "$CLIENT_CONFIG" "$MINI_APP_AUTH"; do
    [ -f "$file" ] || fail "Keycloak session-security file is missing: ${file#"$REPOSITORY_ROOT/"}"
done

grep -Fqx 'revokeRefreshToken: true' "$REALM_CONFIG" ||
    fail "Keycloak must rotate refresh and offline tokens"

grep -Fqx 'refreshTokenMaxReuse: 0' "$REALM_CONFIG" ||
    fail "Rotated Keycloak refresh tokens must not be reusable"

grep -Fqx 'offlineSessionIdleTimeout: 2592000' "$REALM_CONFIG" ||
    fail "Keycloak offline session idle timeout must remain bounded to 30 days"

grep -Fqx 'offlineSessionMaxLifespanEnabled: true' "$REALM_CONFIG" ||
    fail "Keycloak offline sessions must have an absolute maximum lifespan"

grep -Fqx 'offlineSessionMaxLifespan: 5184000' "$REALM_CONFIG" ||
    fail "Keycloak offline session absolute lifespan must remain bounded to 60 days"

grep -Fq '      - offline_access' "$CLIENT_CONFIG" ||
    fail "Mini App must retain the offline_access scope required for long-lived Telegram sessions"

grep -Fq 'const OFFLINE_ACCESS_SCOPE = "offline_access"' "$MINI_APP_AUTH" ||
    fail "Mini App must request the bounded offline_access scope"

echo "Keycloak session security verified: rotating tokens with bounded offline lifetime"
