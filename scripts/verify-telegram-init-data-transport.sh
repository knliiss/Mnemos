#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
MINI_APP_AUTH="$REPOSITORY_ROOT/apps/mini-app/src/features/auth/api/auth.ts"
AUTHENTICATOR="$REPOSITORY_ROOT/keycloak-extensions/telegram-authenticator/src/main/java/com/mnemos/keycloak/telegram/TelegramAuthenticator.java"
LOGIN_TEMPLATE="$REPOSITORY_ROOT/infrastructure/keycloak/themes/mnemos/login/telegram-login.ftl"

fail() {
    echo "$1" >&2
    exit 1
}

for file in "$MINI_APP_AUTH" "$AUTHENTICATOR" "$LOGIN_TEMPLATE"; do
    [ -f "$file" ] || fail "Telegram authentication transport file is missing: ${file#"$REPOSITORY_ROOT/"}"
done

if grep -Fq 'createLoginUrl' "$MINI_APP_AUTH"; then
    fail "Mini App must not construct a Keycloak login URL that can carry Telegram initData"
fi

if grep -Fq 'telegram_init_data' "$MINI_APP_AUTH"; then
    fail "Mini App must not append Telegram initData to the Keycloak login URL"
fi

grep -Fq 'candidate.login({' "$MINI_APP_AUTH" ||
    fail "Mini App must enter the Keycloak login flow through keycloak-js login()"

if grep -Fq 'getQueryParameters()' "$AUTHENTICATOR"; then
    fail "Telegram authenticator must not accept initData from URL query parameters"
fi

grep -Fq 'getDecodedFormParameters()' "$AUTHENTICATOR" ||
    fail "Telegram authenticator must read initData from decoded form parameters"

grep -Fq 'method="post"' "$LOGIN_TEMPLATE" ||
    fail "Telegram login challenge must submit initData with POST"

grep -Fq 'name="telegram_init_data"' "$LOGIN_TEMPLATE" ||
    fail "Telegram login challenge is missing the initData form field"

echo "Telegram initData transport verified: POST body only"
