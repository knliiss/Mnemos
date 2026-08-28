#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKUP_STATE_DIR="${MNEMOS_BACKUP_STATE_DIR:-/var/lib/mnemos-backup}"
LATEST_FILE="$BACKUP_STATE_DIR/latest"

sh "$REPOSITORY_ROOT/scripts/backup-production.sh"

if [ ! -s "$LATEST_FILE" ]; then
    echo "Latest backup state was not written: $LATEST_FILE" >&2
    exit 1
fi

backup_dir="$(cat "$LATEST_FILE")"

sh "$REPOSITORY_ROOT/scripts/verify-production-backup.sh" "$backup_dir"
sh "$REPOSITORY_ROOT/scripts/backup-production-offsite.sh" "$backup_dir"

printf 'Production backup pipeline completed: %s\n' "$backup_dir"
