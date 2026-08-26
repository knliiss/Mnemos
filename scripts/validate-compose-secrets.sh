#!/usr/bin/env sh
set -eu

MIN_SECRET_LENGTH=16

list_required_secret_keys() {
    cat <<'EOF'
APP_POSTGRES_PASSWORD
ACCOUNT_POSTGRES_PASSWORD
WALLET_POSTGRES_PASSWORD
EVENT_POSTGRES_PASSWORD
NOTIFICATION_POSTGRES_PASSWORD
REWARD_POSTGRES_PASSWORD
BET_POSTGRES_PASSWORD
MODERATION_POSTGRES_PASSWORD
REALTIME_POSTGRES_PASSWORD
PERSONALIZATION_POSTGRES_PASSWORD
SOCIAL_POSTGRES_PASSWORD
PROFILE_POSTGRES_PASSWORD
KEYCLOAK_POSTGRES_PASSWORD
KEYCLOAK_ACCOUNT_SERVICE_CLIENT_SECRET
KEYCLOAK_MODERATION_SERVICE_CLIENT_SECRET
KAFKA_BROKER_PASSWORD
KAFKA_ADMIN_PASSWORD
KAFKA_TLS_STORE_PASSWORD
KAFKA_ACCOUNT_SERVICE_PASSWORD
KAFKA_WALLET_SERVICE_PASSWORD
KAFKA_EVENT_SERVICE_PASSWORD
KAFKA_NOTIFICATION_SERVICE_PASSWORD
KAFKA_REWARD_SERVICE_PASSWORD
KAFKA_BET_SERVICE_PASSWORD
KAFKA_MODERATION_SERVICE_PASSWORD
KAFKA_GATEWAY_SERVICE_PASSWORD
KAFKA_REALTIME_SERVICE_PASSWORD
KAFKA_PERSONALIZATION_SERVICE_PASSWORD
KAFKA_SOCIAL_SERVICE_PASSWORD
KAFKA_PROFILE_SERVICE_PASSWORD
REDIS_ADMIN_PASSWORD
REDIS_GATEWAY_PASSWORD
REDIS_REALTIME_PASSWORD
REDIS_NOTIFICATION_PASSWORD
MINIO_ROOT_PASSWORD
MINIO_PERSONALIZATION_SECRET_KEY
MNEMOS_TELEGRAM_BOT_TOKEN
CRISTALIX_ACCOUNT_SERVICE_PROJECT_TOKEN
CRISTALIX_ACCOUNT_SERVICE_PROJECT_KEY
WALLET_SERVICE_ACCOUNT_API_KEY
EVENT_SERVICE_ACCOUNT_API_KEY
REWARD_SERVICE_ACCOUNT_API_KEY
BET_SERVICE_ACCOUNT_API_KEY
BET_SERVICE_EVENT_API_KEY
GATEWAY_MODERATION_API_KEY
MODERATION_SERVICE_WALLET_API_KEY
MNEMOS_REALTIME_FORWARDING_TOKEN
REALTIME_SERVICE_ACCOUNT_API_KEY
PERSONALIZATION_SERVICE_ACCOUNT_API_KEY
SOCIAL_SERVICE_ACCOUNT_API_KEY
SOCIAL_SERVICE_EVENT_API_KEY
SOCIAL_SERVICE_PERSONALIZATION_API_KEY
PROFILE_SERVICE_ACCOUNT_API_KEY
PROFILE_SERVICE_PERSONALIZATION_API_KEY
EOF
}

read_env_value() {
    env_file="$1"
    key="$2"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            value = substr($0, length(key) + 2)
        }
        END {
            print value
        }
    ' "$env_file"
}

normalize_value() {
    printf '%s' "$1" |
        sed \
            -e 's/\r$//' \
            -e 's/^[[:space:]]*//' \
            -e 's/[[:space:]]*$//' \
            -e 's/^"//' \
            -e 's/"$//' \
            -e "s/^'//" \
            -e "s/'$//"
}

fail_secret() {
    key="$1"
    reason="$2"

    echo "Unsafe compose secret $key: $reason" >&2
    exit 1
}

validate_secret() {
    key="$1"
    value="$2"

    if [ -z "$value" ]; then
        fail_secret "$key" "value is missing or blank"
    fi

    case "$value" in
        change_me|change_me_*|replace_me|replace_me_*|changeme|replace-me|replace_me_personalization)
            fail_secret "$key" "placeholder values are not allowed"
            ;;
    esac

    if [ "${#value}" -lt "$MIN_SECRET_LENGTH" ]; then
        fail_secret "$key" "value must contain at least $MIN_SECRET_LENGTH characters"
    fi
}

if [ "${1:-}" = "--list-required" ]; then
    list_required_secret_keys
    exit 0
fi

ENV_FILE="${1:-}"

if [ -z "$ENV_FILE" ]; then
    echo "Usage: $0 <env-file>" >&2
    exit 2
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "Compose env file does not exist: $ENV_FILE" >&2
    exit 1
fi

seen_secret_values=""

for key in $(list_required_secret_keys); do
    raw_value="$(read_env_value "$ENV_FILE" "$key")"
    value="$(normalize_value "$raw_value")"

    validate_secret "$key" "$value"

    if printf '%s' "$seen_secret_values" | grep -Fqx -e "$value"; then
        fail_secret "$key" "value must not be reused by another required secret"
    fi

    seen_secret_values="${seen_secret_values}${value}
"
done

echo "Compose security secrets validated"
