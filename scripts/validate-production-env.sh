#!/usr/bin/env sh

set -eu

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
TARGET_ENV="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Production environment does not exist: $TARGET_ENV" >&2
    exit 1
fi

get_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$TARGET_ENV"
}

require_value() {
    key="$1"
    value="$(get_env "$key")"

    if [ -z "$value" ]; then
        echo "Required production value is missing: $key" >&2
        return 1
    fi
}

EXTERNAL_VALUES="
MNEMOS_TELEGRAM_BOT_TOKEN
MNEMOS_SUPER_ADMIN_TELEGRAM_ID
CRISTALIX_INTERACTIVE_PROJECT_TOKEN
CRISTALIX_INTERACTIVE_PROJECT_KEY
CRISTALIX_MONITORING_PROJECT_TOKEN
CRISTALIX_MONITORING_PROJECT_KEY
CRISTALIX_RESERVE_PROJECT_TOKEN
CRISTALIX_RESERVE_PROJECT_KEY
"

missing=0

for key in $EXTERNAL_VALUES; do
    if ! require_value "$key"; then
        missing=1
    fi
done

if [ "$missing" -ne 0 ]; then
    exit 1
fi

if [ "$(get_env MNEMOS_FRONTEND_ORIGIN)" != "https://app.knalis.rest" ]; then
    echo "MNEMOS_FRONTEND_ORIGIN must be https://app.knalis.rest" >&2
    exit 1
fi

if [ "$(get_env MNEMOS_API_ORIGIN)" != "https://api.knalis.rest" ]; then
    echo "MNEMOS_API_ORIGIN must be https://api.knalis.rest" >&2
    exit 1
fi

if [ "$(get_env MNEMOS_AUTH_ORIGIN)" != "https://auth.knalis.rest" ]; then
    echo "MNEMOS_AUTH_ORIGIN must be https://auth.knalis.rest" >&2
    exit 1
fi

sh "$REPOSITORY_ROOT/scripts/validate-compose-secrets.sh" "$TARGET_ENV"
sh "$REPOSITORY_ROOT/scripts/verify-kafka-message-auth.sh" "$TARGET_ENV"

echo "Production environment is complete and valid"
