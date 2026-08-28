#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

BACKUP_DIR="${1:-}"
CONFIG_FILE="${MNEMOS_BACKUP_CONFIG_FILE:-/etc/mnemos/backup.env}"

if [ -z "$BACKUP_DIR" ]; then
    echo "Usage: $0 <verified-backup-directory>" >&2
    exit 1
fi

if [ ! -d "$BACKUP_DIR" ]; then
    echo "Backup directory does not exist: $BACKUP_DIR" >&2
    exit 1
fi

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Off-site backup configuration is missing: $CONFIG_FILE" >&2
    exit 1
fi

owner="$(stat -c '%U:%G' "$CONFIG_FILE")"
mode="$(stat -c '%a' "$CONFIG_FILE")"

if [ "$owner" != "root:root" ] || [ "$mode" != "600" ]; then
    echo "Off-site backup configuration must be root:root with mode 600" >&2
    exit 1
fi

if ! command -v restic >/dev/null 2>&1; then
    echo "restic is required for encrypted off-site backups" >&2
    exit 1
fi

set -a
# shellcheck disable=SC1090
. "$CONFIG_FILE"
set +a

if [ -z "${RESTIC_REPOSITORY:-}" ]; then
    echo "RESTIC_REPOSITORY is missing from $CONFIG_FILE" >&2
    exit 1
fi

if [ -z "${RESTIC_PASSWORD_FILE:-}" ]; then
    echo "RESTIC_PASSWORD_FILE is missing from $CONFIG_FILE" >&2
    exit 1
fi

if [ ! -f "$RESTIC_PASSWORD_FILE" ]; then
    echo "Restic password file does not exist: $RESTIC_PASSWORD_FILE" >&2
    exit 1
fi

password_owner="$(stat -c '%U:%G' "$RESTIC_PASSWORD_FILE")"
password_mode="$(stat -c '%a' "$RESTIC_PASSWORD_FILE")"

if [ "$password_owner" != "root:root" ] || [ "$password_mode" != "600" ]; then
    echo "Restic password file must be root:root with mode 600" >&2
    exit 1
fi

backup_id="$(basename -- "$BACKUP_DIR")"
host_name="$(hostname --fqdn 2>/dev/null || hostname)"

restic snapshots >/dev/null

restic backup \
    --host "$host_name" \
    --tag mnemos-production \
    --tag "$backup_id" \
    "$BACKUP_DIR" \
    /etc/mnemos

restic forget \
    --host "$host_name" \
    --tag mnemos-production \
    --keep-daily "${MNEMOS_BACKUP_KEEP_DAILY:-7}" \
    --keep-weekly "${MNEMOS_BACKUP_KEEP_WEEKLY:-5}" \
    --keep-monthly "${MNEMOS_BACKUP_KEEP_MONTHLY:-6}" \
    --prune

restic check

printf 'Encrypted off-site backup completed: %s\n' "$backup_id"
