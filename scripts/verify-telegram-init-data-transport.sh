#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
MINI_APP_AUTH="$REPOSITORY_ROOT/apps/mini-app/src/features/auth/api/auth.ts"
LOGIN_TRANSPORT="$REPOSITORY_ROOT/apps/mini-app/src/features/auth/api/telegramLoginTransport.ts"
AUTHENTICATOR="$REPOSITORY_ROOT/keycloak-extensions/telegram-authenticator/src/main/java/com/mnemos/keycloak/telegram/TelegramAuthenticator.java"
LOGIN_TEMPLATE="$REPOSITORY_ROOT/infrastructure/keycloak/themes/mnemos/login/telegram-login.ftl"

fail() {
    echo "$1" >&2
    exit 1
}

for file in "$MINI_APP_AUTH" "$LOGIN_TRANSPORT" "$AUTHENTICATOR" "$LOGIN_TEMPLATE"; do
    [ -f "$file" ] || fail "Telegram authentication transport file is missing: ${file#"$REPOSITORY_ROOT/"}"
done

grep -Fq 'createLoginUrl' "$MINI_APP_AUTH" ||
    fail "Mini App must create the Keycloak login URL before attaching the client-only Telegram credential fragment"

grep -Fq 'createTelegramLoginRedirectUrl' "$MINI_APP_AUTH" ||
    fail "Mini App must use the Telegram login fragment transport"

grep -Fq 'window.location.replace(redirectUrl)' "$MINI_APP_AUTH" ||
    fail "Mini App must replace the current location with the protected Telegram login redirect"

if grep -Eq 'searchParams\.(set|append).*telegram_init_data' "$MINI_APP_AUTH" "$LOGIN_TRANSPORT"; then
    fail "Mini App must not place Telegram initData in Keycloak authorization query parameters"
fi

if grep -Fq '?telegram_init_data' "$MINI_APP_AUTH" "$LOGIN_TRANSPORT"; then
    fail "Mini App must not place Telegram initData in the Keycloak authorization query string"
fi

grep -Fq 'url.hash = fragmentParameters.toString()' "$LOGIN_TRANSPORT" ||
    fail "Telegram initData must cross the app-to-Keycloak navigation only in a client-side URL fragment"

if grep -Fq 'getQueryParameters()' "$AUTHENTICATOR"; then
    fail "Telegram authenticator must not accept initData from URL query parameters"
fi

grep -Fq 'getDecodedFormParameters()' "$AUTHENTICATOR" ||
    fail "Telegram authenticator must read initData from decoded form parameters"

grep -Fq 'method="post"' "$LOGIN_TEMPLATE" ||
    fail "Telegram login challenge must submit initData with POST"

grep -Fq 'name="telegram_init_data"' "$LOGIN_TEMPLATE" ||
    fail "Telegram login challenge is missing the initData form field"

grep -Fq 'window.location.hash' "$LOGIN_TEMPLATE" ||
    fail "Telegram login challenge must recover initData from the client-only URL fragment"

grep -Fq 'window.history.replaceState' "$LOGIN_TEMPLATE" ||
    fail "Telegram login challenge must clear initData from the browser URL before submitting it"

if grep -Fq 'telegram-web-app.js' "$LOGIN_TEMPLATE"; then
    fail "Keycloak login challenge must not depend on a third-party Telegram script to recover initData"
fi

echo "Telegram initData transport verified: client-only fragment to POST body"
