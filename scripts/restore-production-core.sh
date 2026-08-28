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

if [ "${MNEMOS_RESTORE_CONFIRM:-}" != "$backup_id" ]; then
    echo "Restore confirmation is required." >&2
    echo "Set MNEMOS_RESTORE_CONFIRM=$backup_id for this exact backup." >&2
    exit 1
fi

if docker stack services -q "$STACK_NAME" 2>/dev/null | grep -q .; then
    echo "Application stack $STACK_NAME is still deployed." >&2
    echo "Remove or stop application traffic before restoring persistent data." >&2
    exit 1
fi

sh "$REPOSITORY_ROOT/scripts/verify-production-backup.sh" "$BACKUP_DIR"

echo "Restoring application PostgreSQL database"
cat "$BACKUP_DIR/app-postgres.dump" |
    sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
        exec -T app-postgres \
        sh -c 'exec pg_restore --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --clean --if-exists --no-owner --no-acl'

echo "Reapplying application PostgreSQL role ownership"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    run \
    --rm \
    --no-deps \
    app-postgres-security-init

echo "Stopping Keycloak before database restore"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" stop keycloak

echo "Restoring Keycloak PostgreSQL database"
cat "$BACKUP_DIR/keycloak-postgres.dump" |
    sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
        exec -T keycloak-postgres \
        sh -c 'exec pg_restore --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --clean --if-exists --no-owner --no-acl'

echo "Starting restored Keycloak"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    up \
    -d \
    --wait \
    keycloak

echo "Reapplying declarative Keycloak configuration"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    run \
    --rm \
    --no-deps \
    keycloak-config

echo "Ensuring MinIO bucket and service account exist"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    run \
    --rm \
    --no-deps \
    minio-init

echo "Restoring MinIO personalization objects"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    run \
    --rm \
    --no-deps \
    --entrypoint /bin/sh \
    --volume "$BACKUP_DIR/minio:/backup:ro" \
    minio-init \
    -c '
        set -eu
        mc alias set mnemos http://minio:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" >/dev/null
        exec mc mirror --overwrite --preserve /backup/personalization "mnemos/$MINIO_PERSONALIZATION_BUCKET"
    '

cat <<EOF
Core production data restored from:
  $BACKUP_DIR

Redis was not restored automatically. Its RDB snapshot remains at:
  $BACKUP_DIR/redis.rdb

Run the dedicated Redis restore only when queue continuity is required.
Validate infrastructure and Keycloak before redeploying application services.
EOF
