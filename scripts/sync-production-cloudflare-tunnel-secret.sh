#!/usr/bin/env sh

set -eu

TOKEN_FILE="${MNEMOS_CLOUDFLARE_TUNNEL_TOKEN_FILE:-/etc/mnemos/cloudflare-tunnel.token}"
SECRET_PREFIX="${MNEMOS_CLOUDFLARE_TUNNEL_SECRET_PREFIX:-mnemos-cloudflare-tunnel-token}"

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

if [ ! -f "$TOKEN_FILE" ]; then
    echo "Cloudflare Tunnel token file is missing: $TOKEN_FILE" >&2
    exit 1
fi

owner="$(stat -c '%U:%G' "$TOKEN_FILE")"
mode="$(stat -c '%a' "$TOKEN_FILE")"

if [ "$owner" != "root:root" ] || [ "$mode" != "600" ]; then
    echo "Cloudflare Tunnel token file must be owned by root:root with mode 600" >&2
    exit 1
fi

token="$(tr -d '\r\n' < "$TOKEN_FILE")"

if [ -z "$token" ]; then
    echo "Cloudflare Tunnel token is empty" >&2
    exit 1
fi

case "$token" in
    *[!A-Za-z0-9._=-]*)
        echo "Cloudflare Tunnel token contains unexpected characters" >&2
        exit 1
        ;;
esac

secret_hash="$(printf '%s' "$token" | sha256sum | awk '{ print substr($1, 1, 16) }')"
secret_name="${SECRET_PREFIX}-${secret_hash}"

if ! docker secret inspect "$secret_name" >/dev/null 2>&1; then
    printf '%s' "$token" | docker secret create "$secret_name" - >/dev/null
fi

printf '%s\n' "$secret_name"
