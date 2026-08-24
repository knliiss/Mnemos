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
    fail "Mini App must create the Keycloak login URL before submitting the Telegram credential"

grep -Fq 'submitTelegramLoginRequest' "$MINI_APP_AUTH" ||
    fail "Mini App must submit the Telegram login request through the protected POST transport"

grep -Fq 'resolveTelegramSessionUserId' "$MINI_APP_AUTH" ||
    fail "Mini App must resolve the current Telegram identity before accepting a restored Keycloak session"

grep -Fq 'hasMatchingTelegramIdentity(candidate, telegramUserId)' "$MINI_APP_AUTH" ||
    fail "Mini App must compare the restored Keycloak session with the resolved Telegram identity"

grep -Fq 'tokenParsed?.telegram_id' "$MINI_APP_AUTH" ||
    fail "Mini App must bind restored Keycloak sessions to the Telegram identity claim"

grep -Fq 'prompt: "login"' "$MINI_APP_AUTH" ||
    fail "Explicit Telegram login must force re-authentication instead of accepting a stale Keycloak SSO cookie"

if grep -Eq 'searchParams\.(set|append).*telegram_init_data' "$MINI_APP_AUTH" "$LOGIN_TRANSPORT"; then
    fail "Mini App must not place Telegram initData in Keycloak authorization query parameters"
fi

if grep -Fq '?telegram_init_data' "$MINI_APP_AUTH" "$LOGIN_TRANSPORT"; then
    fail "Mini App must not place Telegram initData in the Keycloak authorization query string"
fi

grep -Fq 'parameters.set(TELEGRAM_INIT_DATA_PARAMETER, initData)' "$LOGIN_TRANSPORT" ||
    fail "Telegram initData must be included in the authorization POST body"

grep -Fq 'form.method = "post"' "$LOGIN_TRANSPORT" ||
    fail "Telegram authorization transport must use POST"

grep -Fq 'url.search = ""' "$LOGIN_TRANSPORT" ||
    fail "Telegram authorization transport must remove the query from the form action"

grep -Fq 'url.hash = ""' "$LOGIN_TRANSPORT" ||
    fail "Telegram authorization transport must remove fragments from the form action"

grep -Fq 'form.submit()' "$LOGIN_TRANSPORT" ||
    fail "Telegram authorization transport must submit the generated form"

if grep -Fq 'getQueryParameters()' "$AUTHENTICATOR"; then
    fail "Telegram authenticator must not accept initData from URL query parameters"
fi

grep -Fq 'AuthorizationEndpoint.LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX' "$AUTHENTICATOR" ||
    fail "Telegram authenticator must read initData from Keycloak additional authorization request notes"

grep -Fq 'getClientNote(noteName)' "$AUTHENTICATOR" ||
    fail "Telegram authenticator must consume the stored authorization request parameter"

grep -Fq 'removeClientNote(noteName)' "$AUTHENTICATOR" ||
    fail "Telegram authenticator must remove Telegram initData from the authentication session after consumption"

grep -Fq 'getDecodedFormParameters()' "$AUTHENTICATOR" ||
    fail "Telegram authenticator fallback action must read initData from decoded form parameters"

if grep -Fq 'telegram-web-app.js' "$LOGIN_TEMPLATE"; then
    fail "Keycloak login challenge must not depend on a third-party Telegram script"
fi

echo "Telegram initData transport verified: OIDC POST body to one-time Keycloak authentication session note"
