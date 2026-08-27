#!/usr/bin/env sh

set -eu

STACK_NAME="${1:-}"
RELEASE_TAG="${2:-}"
TIMEOUT_SECONDS="${3:-600}"
POLL_SECONDS="${MNEMOS_DEPLOY_POLL_SECONDS:-5}"

if [ -z "$STACK_NAME" ] || [ -z "$RELEASE_TAG" ]; then
    echo "Usage: $0 <stack-name> <release-tag> [timeout-seconds]" >&2
    exit 1
fi

case "$TIMEOUT_SECONDS" in
    '' | *[!0-9]*)
        echo "timeout-seconds must be a positive integer" >&2
        exit 1
        ;;
esac

if [ "$TIMEOUT_SECONDS" -le 0 ]; then
    echo "timeout-seconds must be greater than zero" >&2
    exit 1
fi

print_diagnostics() {
    echo "===== STACK SERVICES =====" >&2
    docker stack services "$STACK_NAME" 2>&1 >&2 || true

    for service_id in $(docker stack services -q "$STACK_NAME" 2>/dev/null); do
        service_name="$(docker service inspect --format '{{ .Spec.Name }}' "$service_id" 2>/dev/null || true)"

        if [ -z "$service_name" ]; then
            continue
        fi

        echo "===== ${service_name} TASKS =====" >&2
        docker service ps \
            --no-trunc \
            --format 'table {{.Name}}\t{{.Image}}\t{{.CurrentState}}\t{{.Error}}' \
            "$service_id" 2>&1 >&2 || true
    done
}

service_is_ready() {
    service_id="$1"

    replicas="$(
        docker service ls \
            --filter "id=$service_id" \
            --format '{{ .Replicas }}'
    )"

    actual="${replicas%%/*}"
    desired="${replicas##*/}"

    if [ -z "$actual" ] || [ -z "$desired" ] || [ "$actual" != "$desired" ]; then
        return 1
    fi

    update_state="$(
        docker service inspect \
            --format '{{ if .UpdateStatus }}{{ .UpdateStatus.State }}{{ else }}stable{{ end }}' \
            "$service_id"
    )"

    case "$update_state" in
        stable | completed)
            ;;
        *)
            return 1
            ;;
    esac

    service_name="$(
        docker service inspect \
            --format '{{ .Spec.Name }}' \
            "$service_id"
    )"

    if [ "$service_name" = "${STACK_NAME}_cloudflared" ]; then
        return 0
    fi

    image="$(
        docker service inspect \
            --format '{{ .Spec.TaskTemplate.ContainerSpec.Image }}' \
            "$service_id"
    )"

    case "$image" in
        *:"$RELEASE_TAG" | *:"$RELEASE_TAG"@sha256:*)
            ;;
        *)
            echo "Service did not converge to release ${RELEASE_TAG}: image=${image}" >&2
            return 2
            ;;
    esac

    return 0
}

started_at="$(date +%s)"
stable_since=""

while true; do
    service_ids="$(docker stack services -q "$STACK_NAME" 2>/dev/null || true)"

    if [ -z "$service_ids" ]; then
        ready=false
    else
        ready=true

        for service_id in $service_ids; do
            if ! service_is_ready "$service_id"; then
                ready=false
                break
            fi
        done
    fi

    now="$(date +%s)"

    if [ "$ready" = "true" ]; then
        if [ -z "$stable_since" ]; then
            stable_since="$now"
        fi

        if [ $((now - stable_since)) -ge 15 ]; then
            echo "Production stack converged to release ${RELEASE_TAG}"
            exit 0
        fi
    else
        stable_since=""
    fi

    if [ $((now - started_at)) -ge "$TIMEOUT_SECONDS" ]; then
        echo "Production stack did not converge within ${TIMEOUT_SECONDS}s" >&2
        print_diagnostics
        exit 1
    fi

    sleep "$POLL_SECONDS"
done
