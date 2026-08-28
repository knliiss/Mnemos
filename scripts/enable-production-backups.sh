#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

RUNTIME_ROOT="${MNEMOS_RUNTIME_CURRENT_LINK:-/opt/mnemos/current}"
CONFIG_FILE="${MNEMOS_BACKUP_CONFIG_FILE:-/etc/mnemos/backup.env}"
SERVICE_SOURCE="$RUNTIME_ROOT/infrastructure/systemd/mnemos-production-backup.service"
TIMER_SOURCE="$RUNTIME_ROOT/infrastructure/systemd/mnemos-production-backup.timer"
BACKUP_MODE="local"

if [ ! -f "$SERVICE_SOURCE" ] || [ ! -f "$TIMER_SOURCE" ]; then
    echo "Production runtime does not contain backup systemd units" >&2
    exit 1
fi

if [ -f "$CONFIG_FILE" ]; then
    owner="$(stat -c '%U:%G' "$CONFIG_FILE")"
    mode="$(stat -c '%a' "$CONFIG_FILE")"

    if [ "$owner" != "root:root" ] || [ "$mode" != "600" ]; then
        echo "Backup configuration must be root:root with mode 600" >&2
        exit 1
    fi

    set -a
    # shellcheck disable=SC1090
    . "$CONFIG_FILE"
    set +a

    BACKUP_MODE="${MNEMOS_BACKUP_MODE:-local}"
fi

case "$BACKUP_MODE" in
    local)
        ;;
    offsite)
        if [ -z "${RESTIC_REPOSITORY:-}" ] || [ -z "${RESTIC_PASSWORD_FILE:-}" ]; then
            echo "RESTIC_REPOSITORY and RESTIC_PASSWORD_FILE are required for offsite backups" >&2
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

        restic snapshots >/dev/null
        restic check
        ;;
    *)
        echo "MNEMOS_BACKUP_MODE must be local or offsite" >&2
        exit 1
        ;;
esac

install -o root -g root -m 0644 \
    "$SERVICE_SOURCE" \
    /etc/systemd/system/mnemos-production-backup.service
install -o root -g root -m 0644 \
    "$TIMER_SOURCE" \
    /etc/systemd/system/mnemos-production-backup.timer

systemctl daemon-reload
systemctl enable --now mnemos-production-backup.timer
systemctl status --no-pager mnemos-production-backup.timer

printf 'Production backups enabled in %s mode\n' "$BACKUP_MODE"
