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

if requires_secret_validation "$@"; then
    sh "$SCRIPT_DIR/ensure-kafka-tls-secret.sh" "$TARGET_ENV"
    sh "$SCRIPT_DIR/validate-compose-secrets.sh" "$TARGET_ENV"
fi

for key in $(sed -n 's/^\([A-Za-z_][A-Za-z0-9_]*\)=.*/\1/p' "$TARGET_ENV"); do
    unset "$key"
done

exec docker compose --env-file "$TARGET_ENV" "$@"
