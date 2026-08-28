#!/usr/bin/env sh

set -eu

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-mnemos}"

INFRA_CONTAINERS="
${COMPOSE_PROJECT_NAME}-app-postgres
${COMPOSE_PROJECT_NAME}-keycloak-postgres
${COMPOSE_PROJECT_NAME}-redis
${COMPOSE_PROJECT_NAME}-kafka
${COMPOSE_PROJECT_NAME}-minio
${COMPOSE_PROJECT_NAME}-keycloak
"

for container_name in $INFRA_CONTAINERS; do
    if ! docker inspect "$container_name" >/dev/null 2>&1; then
        echo "Required production container is missing: $container_name" >&2
        exit 1
    fi

    running="$(docker inspect --format '{{ .State.Running }}' "$container_name")"

    if [ "$running" != "true" ]; then
        echo "Required production container is not running: $container_name" >&2
        exit 1
    fi

    health="$(docker inspect --format '{{ if .State.Health }}{{ .State.Health.Status }}{{ else }}none{{ end }}' "$container_name")"

    case "$health" in
        healthy | none)
            ;;
        *)
            echo "Required production container is not healthy: $container_name ($health)" >&2
            exit 1
            ;;
    esac

done

echo "Production infrastructure is running and healthy"
