#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"
SWARM_NETWORK="${MNEMOS_SWARM_NETWORK:-mnemos-production-network}"
WAIT_TIMEOUT_SECONDS="${MNEMOS_PRODUCTION_INFRA_TIMEOUT_SECONDS:-600}"
POLL_INTERVAL_SECONDS=5

case "$WAIT_TIMEOUT_SECONDS" in
    '' | *[!0-9]*)
        echo "MNEMOS_PRODUCTION_INFRA_TIMEOUT_SECONDS must be a positive integer" >&2
        exit 1
        ;;
esac

if [ "$WAIT_TIMEOUT_SECONDS" -le 0 ]; then
    echo "MNEMOS_PRODUCTION_INFRA_TIMEOUT_SECONDS must be greater than zero" >&2
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    echo "Production environment file is missing: $ENV_FILE" >&2
    exit 1
fi

sh "$REPOSITORY_ROOT/scripts/validate-production-env.sh"

swarm_state="$(docker info --format '{{ .Swarm.LocalNodeState }}')"
manager_control="$(docker info --format '{{ .Swarm.ControlAvailable }}')"

if [ "$swarm_state" != "active" ] || [ "$manager_control" != "true" ]; then
    echo "This node is not an active Docker Swarm manager" >&2
    exit 1
fi

network_driver="$(docker network inspect --format '{{ .Driver }}' "$SWARM_NETWORK" 2>/dev/null || true)"
network_scope="$(docker network inspect --format '{{ .Scope }}' "$SWARM_NETWORK" 2>/dev/null || true)"
network_attachable="$(docker network inspect --format '{{ .Attachable }}' "$SWARM_NETWORK" 2>/dev/null || true)"

if [ "$network_driver" != "overlay" ] || [ "$network_scope" != "swarm" ] || [ "$network_attachable" != "true" ]; then
    echo "Production network must be an attachable Swarm overlay: $SWARM_NETWORK" >&2
    exit 1
fi

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-mnemos}"
export COMPOSE_PROJECT_NAME

production_compose() {
    sh "$REPOSITORY_ROOT/scripts/production-infra.sh" "$@"
}

print_diagnostics() {
    echo "=== Production infrastructure state ===" >&2
    production_compose ps -a >&2 || true

    for service in \
        app-postgres-security-init \
        kafka-tls-init \
        kafka-init \
        minio-init \
        keycloak-config; do
        echo "=== ${service} logs ===" >&2
        production_compose logs --no-color --tail=100 "$service" >&2 || true
    done
}

one_shot_state() {
    service="$1"
    container_id="$(production_compose ps -aq "$service" | head -n 1)"

    if [ -z "$container_id" ]; then
        return 1
    fi

    state="$(docker inspect --format '{{ .State.Status }}' "$container_id")"
    exit_code="$(docker inspect --format '{{ .State.ExitCode }}' "$container_id")"

    case "$state" in
        exited)
            if [ "$exit_code" = "0" ]; then
                return 0
            fi

            echo "Production initialization service failed: ${service} (exit=${exit_code})" >&2
            return 2
            ;;
        dead)
            echo "Production initialization service is dead: $service" >&2
            return 2
            ;;
        created | running | restarting)
            return 1
            ;;
        *)
            echo "Unexpected production initialization service state: ${service} (${state})" >&2
            return 2
            ;;
    esac
}

echo "Starting or updating production infrastructure"
production_compose up -d --remove-orphans

deadline="$(( $(date +%s) + WAIT_TIMEOUT_SECONDS ))"

while :; do
    long_lived_ready=false

    if sh "$REPOSITORY_ROOT/scripts/verify-production-infra.sh" >/dev/null 2>&1; then
        long_lived_ready=true
    fi

    one_shots_ready=true

    for service in \
        app-postgres-security-init \
        kafka-tls-init \
        kafka-init \
        minio-init \
        keycloak-config; do
        set +e
        one_shot_state "$service"
        state_status=$?
        set -e

        case "$state_status" in
            0)
                ;;
            1)
                one_shots_ready=false
                ;;
            *)
                print_diagnostics
                exit 1
                ;;
        esac
    done

    if [ "$long_lived_ready" = "true" ] && [ "$one_shots_ready" = "true" ]; then
        break
    fi

    if [ "$(date +%s)" -ge "$deadline" ]; then
        echo "Production infrastructure did not become ready within ${WAIT_TIMEOUT_SECONDS}s" >&2
        print_diagnostics
        exit 1
    fi

    sleep "$POLL_INTERVAL_SECONDS"
done

sh "$REPOSITORY_ROOT/scripts/verify-production-infra.sh"
echo "Production infrastructure startup completed successfully"
