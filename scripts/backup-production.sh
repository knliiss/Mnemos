#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKUP_ROOT="${MNEMOS_BACKUP_ROOT:-/var/backups/mnemos}"
BACKUP_STATE_DIR="${MNEMOS_BACKUP_STATE_DIR:-/var/lib/mnemos-backup}"
LOCAL_LATEST_FILE="$BACKUP_STATE_DIR/latest-local"
TIMESTAMP="$(date -u '+%Y%m%dT%H%M%SZ')"
BACKUP_DIR="$BACKUP_ROOT/$TIMESTAMP"
MINIO_BACKUP_DIR="$BACKUP_DIR/minio"
REDIS_TEMP_FILE="/tmp/mnemos-backup-${TIMESTAMP}.rdb"

install -d -o root -g root -m 0700 "$BACKUP_ROOT"
install -d -o root -g root -m 0700 "$BACKUP_DIR"
install -d -o root -g root -m 0700 "$MINIO_BACKUP_DIR"
install -d -o root -g root -m 0700 "$BACKUP_STATE_DIR"

cleanup() {
    sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
        exec -T redis \
        rm -f "$REDIS_TEMP_FILE" \
        >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

echo "Creating application PostgreSQL backup"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    exec -T app-postgres \
    sh -c 'exec pg_dump --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --format=custom --compress=6 --no-owner --no-acl' \
    > "$BACKUP_DIR/app-postgres.dump"

echo "Creating Keycloak PostgreSQL backup"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    exec -T keycloak-postgres \
    sh -c 'exec pg_dump --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --format=custom --compress=6 --no-owner --no-acl' \
    > "$BACKUP_DIR/keycloak-postgres.dump"

echo "Creating Redis RDB backup"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    exec -T redis \
    sh -c 'REDISCLI_AUTH="$REDIS_ADMIN_PASSWORD" exec redis-cli --user redis_admin --rdb "$1"' \
    sh "$REDIS_TEMP_FILE" \
    >/dev/null

redis_container="$(
    sh "$REPOSITORY_ROOT/scripts/production-infra.sh" ps -q redis
)"

if [ -z "$redis_container" ]; then
    echo "Redis production container is not running" >&2
    exit 1
fi

docker cp "$redis_container:$REDIS_TEMP_FILE" "$BACKUP_DIR/redis.rdb"

echo "Creating MinIO object backup"
sh "$REPOSITORY_ROOT/scripts/production-infra.sh" \
    run \
    --rm \
    --no-deps \
    --entrypoint /bin/sh \
    --volume "$MINIO_BACKUP_DIR:/backup" \
    minio-init \
    -c '
        set -eu
        mc alias set mnemos http://minio:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" >/dev/null
        mkdir -p /backup/personalization
        exec mc mirror --preserve "mnemos/$MINIO_PERSONALIZATION_BUCKET" /backup/personalization
    '

release="unknown"

if [ -f /var/lib/mnemos-deploy/current-release ]; then
    release="$(cat /var/lib/mnemos-deploy/current-release)"
fi

cat > "$BACKUP_DIR/metadata.txt" <<EOF
created_at_utc=$TIMESTAMP
release=$release
postgres_format=custom
redis_format=rdb
minio_bucket=personalization
secrets_included=false
EOF

(
    cd "$BACKUP_DIR"
    find . -type f ! -name manifest.sha256 -print0 \
        | sort -z \
        | xargs -0 sha256sum \
        > manifest.sha256
)

chmod -R go-rwx "$BACKUP_DIR"

sh "$REPOSITORY_ROOT/scripts/verify-production-backup.sh" "$BACKUP_DIR"

printf '%s\n' "$BACKUP_DIR" > "$LOCAL_LATEST_FILE"
chmod 600 "$LOCAL_LATEST_FILE"

printf 'Production backup created and locally verified: %s\n' "$BACKUP_DIR"
