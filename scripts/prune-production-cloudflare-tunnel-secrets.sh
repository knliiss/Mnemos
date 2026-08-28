#!/usr/bin/env sh

set -eu

CURRENT_SECRET="${1:-}"
SECRET_PREFIX="${MNEMOS_CLOUDFLARE_TUNNEL_SECRET_PREFIX:-mnemos-cloudflare-tunnel-token}"

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

if [ -z "$CURRENT_SECRET" ]; then
    echo "Usage: $0 <current-secret-name>" >&2
    exit 1
fi

for secret_name in $(docker secret ls --format '{{ .Name }}'); do
    case "$secret_name" in
        "${SECRET_PREFIX}-"*)
            ;;
        *)
            continue
            ;;
    esac

    if [ "$secret_name" = "$CURRENT_SECRET" ]; then
        continue
    fi

    if docker secret rm "$secret_name" >/dev/null 2>&1; then
        echo "Removed obsolete Cloudflare Tunnel secret: $secret_name"
    else
        echo "Retaining Cloudflare Tunnel secret still referenced by a service: $secret_name" >&2
    fi
done
