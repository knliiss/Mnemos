#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
APP_ROOT="$REPOSITORY_ROOT/apps/mini-app"
SOURCE_HEADERS="$APP_ROOT/public/_headers"
DIST_HEADERS="$APP_ROOT/dist/_headers"

fail() {
    echo "$1" >&2
    exit 1
}

require_contains() {
    value="$1"
    expected="$2"
    description="$3"

    printf '%s\n' "$value" | grep -Fq "$expected" ||
        fail "Mini App security headers are missing $description"
}

[ -f "$SOURCE_HEADERS" ] || fail "Mini App Cloudflare Pages _headers file is missing"

if awk 'length($0) > 2000 { exit 1 }' "$SOURCE_HEADERS"; then
    :
else
    fail "Mini App _headers contains a line longer than the Cloudflare Pages limit"
fi

csp="$(
    sed -n 's/^[[:space:]]*Content-Security-Policy:[[:space:]]*//p' "$SOURCE_HEADERS"
)"

[ -n "$csp" ] || fail "Mini App Content-Security-Policy header is missing"

script_src="$(
    printf '%s\n' "$csp" |
        tr ';' '\n' |
        sed -n 's/^[[:space:]]*script-src[[:space:]]*/script-src /p'
)"

if [ "$script_src" != "script-src 'self' https://telegram.org" ]; then
    fail "Mini App script-src must allow only self and the official Telegram Web App script origin"
fi

if printf '%s\n' "$script_src" | grep -Eq "'unsafe-inline'|'unsafe-eval'|(^|[[:space:]])\*([[:space:]]|$)"; then
    fail "Mini App script-src contains an unsafe execution source"
fi

require_contains "$csp" "default-src 'self'" "a self-only default source"
require_contains "$csp" "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com" "the required style sources"
require_contains "$csp" "img-src 'self' data: blob: https://api.knalis.rest https://webdata.c7x.dev https://rustorage.c7x.dev" "the required image sources including canonical SAO PET textures"
require_contains "$csp" "connect-src 'self' https://api.knalis.rest https://auth.knalis.rest" "the production API and Keycloak connection sources"
require_contains "$csp" "font-src 'self' data: https://fonts.gstatic.com" "the required font sources"
require_contains "$csp" "frame-src 'self' https://auth.knalis.rest" "the Keycloak silent SSO frame source"
require_contains "$csp" "frame-ancestors 'self' https://telegram.org https://*.telegram.org" "same-origin silent SSO and Telegram framing"
require_contains "$csp" "object-src 'none'" "object blocking"
require_contains "$csp" "base-uri 'none'" "base URI blocking"
require_contains "$csp" "form-action 'self' https://auth.knalis.rest" "the Mini App and production Keycloak form targets"
require_contains "$csp" "upgrade-insecure-requests" "insecure-request upgrading"

required_headers="
X-Content-Type-Options: nosniff
Referrer-Policy: no-referrer
Permissions-Policy: camera=(), microphone=(), geolocation=(), payment=(), usb=(), serial=()
Strict-Transport-Security: max-age=31536000; includeSubDomains
"

printf '%s\n' "$required_headers" |
    while IFS= read -r header; do
        [ -n "$header" ] || continue

        grep -Fq "$header" "$SOURCE_HEADERS" ||
            fail "Mini App security header is missing: $header"
    done

if [ -d "$APP_ROOT/dist" ]; then
    [ -f "$DIST_HEADERS" ] || fail "Mini App build did not copy _headers into dist"

    cmp -s "$SOURCE_HEADERS" "$DIST_HEADERS" ||
        fail "Built Mini App _headers differs from the reviewed source file"
fi

echo "Mini App browser security headers verified"
