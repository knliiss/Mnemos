#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKUP_DIR="${1:-}"
STACK_NAME="${MNEMOS_SWARM_STACK_NAME:-mnemos}"

if [ -z "$BACKUP_DIR" ]; then
    echo "Usage: $0 <backup-directory>" >&2
    exit 1
fi

backup_id="$(basename -- "$BACKUP_DIR")"

if [ "${MNEMOS_RESTORE_REDIS_CONFIRM:-}" != "$backup_id" ]; then
    echo "Redis restore confirmation is required." >&2
    echo "Set MNEMOS_RESTORE_REDIS_CONFIRM=$backup_id for this exact backup." >&2
    exit 1
fi

if docker stack services -q "$STACK_NAME" 2>/dev/null | grep -q .; then
    echo "Application stack $STACK_NAME is still deployed." >&2
    echo "Remove the application stack before restoring Redis." >&2
    exit 1
fi

sh "$REPOSITORY_ROOT/scripts/verify-production-backup.sh" "$BACKUP_DIR"

echo "Stopping production Redis"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" stop redis

echo "Replacing Redis persistence with the verified RDB snapshot"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    run \
    --rm \
    --no-deps \
    --entrypoint /bin/sh \
    --volume "$BACKUP_DIR:/backup:ro" \
    redis \
    -c '
        set -eu
        rm -rf /data/appendonlydir
        rm -f /data/dump.rdb
        cp /backup/redis.rdb /data/dump.rdb
        chown redis:redis /data/dump.rdb
        chmod 600 /data/dump.rdb
    '

echo "Starting restored Redis and waiting for health"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    up \
    -d \
    --wait \
    redis

sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    exec -T redis \
    sh -c 'REDISCLI_AUTH="$REDIS_ADMIN_PASSWORD" redis-cli --user redis_admin ping | grep -q PONG'

printf 'Redis production state restored from: %s\n' "$BACKUP_DIR/redis.rdb"
