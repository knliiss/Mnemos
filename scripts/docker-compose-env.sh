#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
TARGET_ENV="${1:-}"

if [ -z "$TARGET_ENV" ]; then
    echo "Usage: $0 <env-file> [docker compose arguments...]" >&2
    exit 2
fi

shift

if [ ! -f "$TARGET_ENV" ]; then
    echo "Target env file does not exist: $TARGET_ENV" >&2
    exit 1
fi

requires_secret_validation() {
    for argument in "$@"; do
        case "$argument" in
            up|start|restart|create|run)
                return 0
                ;;
        esac
    done

    return 1
}

is_managed_kafka_environment_key() {
    case "$1" in
        MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS|*_KAFKA_SIGNING_PRIVATE_KEY|KAFKA_*_PASSWORD)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

if requires_secret_validation "$@"; then
    sh "$SCRIPT_DIR/ensure-kafka-tls-secret.sh" "$TARGET_ENV"
    sh "$SCRIPT_DIR/validate-compose-secrets.sh" "$TARGET_ENV"
fi

for key in $(env | sed -n 's/=.*//p'); do
    if is_managed_kafka_environment_key "$key"; then
        unset "$key"
    fi
done

exec docker compose --env-file "$TARGET_ENV" "$@"
