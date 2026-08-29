#!/usr/bin/env sh

set -eu

CONTAINER_NAME="${1:-}"
EXPECTED_IMAGE="${2:-}"
TIMEOUT_SECONDS="${3:-300}"
STABLE_SECONDS="${4:-15}"
POLL_SECONDS="${MNEMOS_DEPLOY_POLL_SECONDS:-5}"

if [ -z "$CONTAINER_NAME" ] || [ -z "$EXPECTED_IMAGE" ]; then
    echo "Usage: $0 <container-name> <expected-image> [timeout-seconds] [stable-seconds]" >&2
    exit 1
fi

for value_name in TIMEOUT_SECONDS STABLE_SECONDS POLL_SECONDS; do
    eval "value=\${$value_name}"

    case "$value" in
        '' | *[!0-9]*)
            echo "$value_name must be a positive integer" >&2
            exit 1
            ;;
    esac

    if [ "$value" -le 0 ]; then
        echo "$value_name must be greater than zero" >&2
        exit 1
    fi
done

print_diagnostics() {
    echo "===== ${CONTAINER_NAME} INSPECT =====" >&2
    docker inspect "$CONTAINER_NAME" 2>&1 >&2 || true

    echo "===== ${CONTAINER_NAME} LOGS =====" >&2
    docker logs --tail 100 "$CONTAINER_NAME" 2>&1 >&2 || true
}

container_is_ready() {
    if ! docker inspect "$CONTAINER_NAME" >/dev/null 2>&1; then
        return 1
    fi

    status="$(docker inspect --format '{{.State.Status}}' "$CONTAINER_NAME")"

    if [ "$status" != "running" ]; then
        return 1
    fi

    health="$(
        docker inspect \
            --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' \
            "$CONTAINER_NAME"
    )"

    case "$health" in
        healthy | none)
            ;;
        unhealthy)
            echo "Container is unhealthy: $CONTAINER_NAME" >&2
            return 2
            ;;
        *)
            return 1
            ;;
    esac

    actual_image="$(docker inspect --format '{{.Config.Image}}' "$CONTAINER_NAME")"

    if [ "$actual_image" != "$EXPECTED_IMAGE" ]; then
        echo "Container image mismatch: expected=$EXPECTED_IMAGE actual=$actual_image" >&2
        return 2
    fi

    return 0
}

started_at="$(date +%s)"
stable_since=""

while true; do
    if container_is_ready; then
        now="$(date +%s)"

        if [ -z "$stable_since" ]; then
            stable_since="$now"
        fi

        if [ $((now - stable_since)) -ge "$STABLE_SECONDS" ]; then
            echo "Production container healthy: $CONTAINER_NAME -> $EXPECTED_IMAGE"
            exit 0
        fi
    else
        status="$?"
        stable_since=""

        if [ "$status" -eq 2 ]; then
            print_diagnostics
            exit 1
        fi
    fi

    now="$(date +%s)"

    if [ $((now - started_at)) -ge "$TIMEOUT_SECONDS" ]; then
        echo "Production container did not become healthy within ${TIMEOUT_SECONDS}s: $CONTAINER_NAME" >&2
        print_diagnostics
        exit 1
    fi

    sleep "$POLL_SECONDS"
done
