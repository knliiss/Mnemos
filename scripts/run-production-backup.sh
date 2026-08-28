#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BACKUP_MODE="${MNEMOS_BACKUP_MODE:-local}"
BACKUP_STATE_DIR="${MNEMOS_BACKUP_STATE_DIR:-/var/lib/mnemos-backup}"
LOCAL_LATEST_FILE="$BACKUP_STATE_DIR/latest-local"
SUCCESSFUL_LATEST_FILE="$BACKUP_STATE_DIR/latest-successful"
SUCCESSFUL_LATEST_TEMP="$BACKUP_STATE_DIR/.latest-successful.tmp.$$"

cleanup() {
    rm -f "$SUCCESSFUL_LATEST_TEMP"
}

trap cleanup EXIT INT TERM

case "$BACKUP_MODE" in
    local | offsite)
        ;;
    *)
        echo "MNEMOS_BACKUP_MODE must be local or offsite" >&2
        exit 1
        ;;
esac

sh "$REPOSITORY_ROOT/scripts/backup-production.sh"

if [ ! -s "$LOCAL_LATEST_FILE" ]; then
    echo "Latest locally verified backup state was not written: $LOCAL_LATEST_FILE" >&2
    exit 1
fi

backup_dir="$(cat "$LOCAL_LATEST_FILE")"

sh "$REPOSITORY_ROOT/scripts/verify-production-backup.sh" "$backup_dir"

if [ "$BACKUP_MODE" = "offsite" ]; then
    sh "$REPOSITORY_ROOT/scripts/backup-production-offsite.sh" "$backup_dir"
fi

sh "$REPOSITORY_ROOT/scripts/prune-production-local-backups.sh"

printf '%s\n' "$backup_dir" > "$SUCCESSFUL_LATEST_TEMP"
chmod 600 "$SUCCESSFUL_LATEST_TEMP"
mv -f "$SUCCESSFUL_LATEST_TEMP" "$SUCCESSFUL_LATEST_FILE"

printf 'Production backup pipeline completed: mode=%s backup=%s\n' \
    "$BACKUP_MODE" \
    "$backup_dir"
