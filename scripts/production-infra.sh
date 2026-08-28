#!/usr/bin/env sh

set -eu

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"

PRODUCTION_BASE="$REPOSITORY_ROOT/infrastructure/production/compose.base.yml"
PRODUCTION_OVERRIDE="$REPOSITORY_ROOT/infrastructure/production/compose.infra-overrides.yml"

set -- \
    -f "$PRODUCTION_BASE" \
    -f "$REPOSITORY_ROOT/infrastructure/compose/compose.postgres.yml" \
    -f "$REPOSITORY_ROOT/infrastructure/compose/compose.redis.yml" \
    -f "$REPOSITORY_ROOT/infrastructure/compose/compose.kafka.yml" \
    -f "$REPOSITORY_ROOT/infrastructure/compose/compose.minio.yml" \
    -f "$REPOSITORY_ROOT/infrastructure/compose/compose.keycloak.yml" \
    -f "$REPOSITORY_ROOT/infrastructure/compose/compose.keycloak-config.yml" \
    -f "$PRODUCTION_OVERRIDE" \
    "$@"

exec sh "$REPOSITORY_ROOT/scripts/docker-compose-env.sh" "$ENV_FILE" "$@"
