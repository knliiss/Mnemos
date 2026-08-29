#!/usr/bin/env sh

set -eu

SERVICE_NAME="${1:-}"
EXPECTED_IMAGE="${2:-}"
TIMEOUT_SECONDS="${3:-300}"
STABLE_SECONDS="${4:-15}"
POLL_SECONDS="${MNEMOS_DEPLOY_POLL_SECONDS:-5}"

if [ -z "$SERVICE_NAME" ] || [ -z "$EXPECTED_IMAGE" ]; then
    echo "Usage: $0 <service-name> <expected-image> [timeout-seconds] [stable-seconds]" >&2
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
    echo "===== ${SERVICE_NAME} INSPECT =====" >&2
    docker service inspect "$SERVICE_NAME" 2>&1 >&2 || true

    echo "===== ${SERVICE_NAME} TASKS =====" >&2
    docker service ps \
        --no-trunc \
        --format 'table {{.Name}}\t{{.Image}}\t{{.CurrentState}}\t{{.Error}}' \
        "$SERVICE_NAME" 2>&1 >&2 || true
}

image_matches() {
    actual_image="$1"

    case "$actual_image" in
        "$EXPECTED_IMAGE" | "$EXPECTED_IMAGE"@sha256:*)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

service_is_ready() {
    if ! docker service inspect "$SERVICE_NAME" >/dev/null 2>&1; then
        return 1
    fi

    replicas="$(
        docker service ls \
            --filter "name=$SERVICE_NAME" \
            --format '{{.Name}} {{.Replicas}}' \
            | awk -v expected="$SERVICE_NAME" '$1 == expected { print $2; exit }'
    )"

    actual="${replicas%%/*}"
    desired="${replicas##*/}"

    if [ -z "$actual" ] || [ -z "$desired" ] || [ "$actual" != "$desired" ]; then
        return 1
    fi

    update_state="$(
        docker service inspect \
            --format '{{if .UpdateStatus}}{{.UpdateStatus.State}}{{else}}stable{{end}}' \
            "$SERVICE_NAME"
    )"

    case "$update_state" in
        stable | completed)
            ;;
        rollback_completed)
            echo "Service rolled back instead of converging: $SERVICE_NAME" >&2
            return 2
            ;;
        *)
            return 1
            ;;
    esac

    actual_image="$(
        docker service inspect \
            --format '{{.Spec.TaskTemplate.ContainerSpec.Image}}' \
            "$SERVICE_NAME"
    )"

    if ! image_matches "$actual_image"; then
        echo "Service image mismatch: expected=$EXPECTED_IMAGE actual=$actual_image" >&2
        return 2
    fi

    return 0
}

started_at="$(date +%s)"
stable_since=""

while true; do
    if service_is_ready; then
        now="$(date +%s)"

        if [ -z "$stable_since" ]; then
            stable_since="$now"
        fi

        if [ $((now - stable_since)) -ge "$STABLE_SECONDS" ]; then
            echo "Production service converged: $SERVICE_NAME -> $EXPECTED_IMAGE"
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
        echo "Production service did not converge within ${TIMEOUT_SECONDS}s: $SERVICE_NAME" >&2
        print_diagnostics
        exit 1
    fi

    sleep "$POLL_SECONDS"
done
