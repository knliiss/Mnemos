#!/usr/bin/env sh
set -eu

TARGET_ENV=".env"
ALLOW_MISSING=0

for argument in "$@"; do
    case "$argument" in
        --allow-missing)
            ALLOW_MISSING=1
            ;;
        *)
            TARGET_ENV="$argument"
            ;;
    esac
done

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Target env file does not exist: $TARGET_ENV" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required" >&2
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

get_container_env() {
    container_id="$1"
    key="$2"

    docker inspect \
        --format '{{range .Config.Env}}{{println .}}{{end}}' \
        "$container_id" |
        awk -v key="$key" '
            index($0, key "=") == 1 {
                print substr($0, length(key) + 2)
                exit
            }
        '
}

service_variable_name() {
    printf '%s' "$1" | tr '[:lower:]-' '[:upper:]_'
}

discover_services() {
    for compose_file in "$REPOSITORY_ROOT"/infrastructure/compose/apps/compose.*-service.yml; do
        [ -f "$compose_file" ] || continue

        if grep -q 'MNEMOS_KAFKA_MESSAGEAUTH_PRIVATEKEY:' "$compose_file"; then
            basename "$compose_file" | sed -e 's/^compose\.//' -e 's/\.yml$//'
        fi
    done
}

project_name="$(get_env "COMPOSE_PROJECT_NAME")"
trusted_public_keys="$(get_env "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS")"
services="$(discover_services)"

if [ -z "$project_name" ]; then
    echo "COMPOSE_PROJECT_NAME is required" >&2
    exit 1
fi

if [ -z "$trusted_public_keys" ]; then
    echo "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS is required" >&2
    exit 1
fi

verified_count=0

for service in $services; do
    container_id="$(
        docker ps -aq \
            --filter "label=com.docker.compose.project=$project_name" \
            --filter "label=com.docker.compose.service=$service" |
            head -n 1
    )"

    if [ -z "$container_id" ]; then
        if [ "$ALLOW_MISSING" -eq 1 ]; then
            continue
        fi

        echo "Kafka message-authenticated container is missing: $service" >&2
        exit 1
    fi

    state="$(docker inspect --format '{{.State.Status}}' "$container_id")"

    if [ "$state" != "running" ]; then
        if [ "$ALLOW_MISSING" -eq 1 ]; then
            continue
        fi

        echo "Kafka message-authenticated container is not running: $service ($state)" >&2
        exit 1
    fi

    service_variable="$(service_variable_name "$service")"
    expected_private_key="$(get_env "${service_variable}_KAFKA_SIGNING_PRIVATE_KEY")"
    actual_private_key="$(get_container_env "$container_id" "MNEMOS_KAFKA_MESSAGEAUTH_PRIVATEKEY")"
    actual_trusted_public_keys="$(get_container_env "$container_id" "MNEMOS_KAFKA_MESSAGEAUTH_TRUSTEDPUBLICKEYS")"

    if [ "$actual_private_key" != "$expected_private_key" ]; then
        echo "Running $service uses stale Kafka signing-key material; recreate the full stack" >&2
        exit 1
    fi

    if [ "$actual_trusted_public_keys" != "$trusted_public_keys" ]; then
        echo "Running $service uses a stale Kafka trusted-key set; recreate the full stack" >&2
        exit 1
    fi

    verified_count=$((verified_count + 1))
done

echo "Running Kafka message authentication verified for $verified_count services"
