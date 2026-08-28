#!/usr/bin/env sh

set -eu

BACKUP_DIR="${1:-}"

if [ -z "$BACKUP_DIR" ]; then
    echo "Usage: $0 <backup-directory>" >&2
    exit 1
fi

if [ ! -d "$BACKUP_DIR" ]; then
    echo "Backup directory does not exist: $BACKUP_DIR" >&2
    exit 1
fi

for required_file in \
    app-postgres.dump \
    keycloak-postgres.dump \
    redis.rdb \
    metadata.txt \
    manifest.sha256; do
    if [ ! -s "$BACKUP_DIR/$required_file" ]; then
        echo "Backup file is missing or empty: $required_file" >&2
        exit 1
    fi
done

if [ ! -d "$BACKUP_DIR/minio/personalization" ]; then
    echo "MinIO personalization backup directory is missing" >&2
    exit 1
fi

(
    cd "$BACKUP_DIR"
    sha256sum --check --strict manifest.sha256 >/dev/null
)

if command -v pg_restore >/dev/null 2>&1; then
    pg_restore --list "$BACKUP_DIR/app-postgres.dump" >/dev/null
    pg_restore --list "$BACKUP_DIR/keycloak-postgres.dump" >/dev/null
fi

metadata_secrets="$(
    sed -n 's/^secrets_included=//p' "$BACKUP_DIR/metadata.txt"
)"

if [ "$metadata_secrets" != "false" ]; then
    echo "Backup metadata must explicitly state that secrets are not stored in the local snapshot" >&2
    exit 1
fi

printf 'Production backup verified: %s\n' "$BACKUP_DIR"
