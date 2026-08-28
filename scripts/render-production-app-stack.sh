#!/usr/bin/env sh

set -eu

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
COMPOSE_DIR="$REPOSITORY_ROOT/infrastructure/compose"
PRODUCTION_APP_OVERRIDE="$REPOSITORY_ROOT/infrastructure/production/compose.app-overrides.yml"
ENV_FILE="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"
OUTPUT_FILE="${1:-}"

if [ -z "$OUTPUT_FILE" ]; then
    echo "Usage: $0 <output-file>" >&2
    exit 1
fi

umask 077

COMPOSE_MODEL="$(mktemp)"

cleanup() {
    rm -f "$COMPOSE_MODEL"
}

trap cleanup EXIT INT TERM

set -- -f "$COMPOSE_DIR/compose.base.yml"

for compose_file in "$COMPOSE_DIR"/compose.*.yml; do
    if [ "$compose_file" = "$COMPOSE_DIR/compose.base.yml" ]; then
        continue
    fi

    set -- "$@" -f "$compose_file"
done

for compose_file in "$COMPOSE_DIR"/apps/compose.*.yml; do
    set -- "$@" -f "$compose_file"
done

set -- "$@" -f "$PRODUCTION_APP_OVERRIDE"

sh "$REPOSITORY_ROOT/scripts/docker-compose-env.sh" \
    "$ENV_FILE" \
    "$@" \
    config \
    --format json \
    > "$COMPOSE_MODEL"

python3 "$REPOSITORY_ROOT/scripts/render-production-stack.py" \
    --input "$COMPOSE_MODEL" \
    --output "$OUTPUT_FILE"

chmod 600 "$OUTPUT_FILE"
