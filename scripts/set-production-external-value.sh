#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

KEY="${1:-}"
TARGET_ENV="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"

case "$KEY" in
    MNEMOS_TELEGRAM_BOT_TOKEN | \
    MNEMOS_SUPER_ADMIN_TELEGRAM_ID | \
    CRISTALIX_INTERACTIVE_PROJECT_TOKEN | \
    CRISTALIX_INTERACTIVE_PROJECT_KEY | \
    CRISTALIX_MONITORING_PROJECT_TOKEN | \
    CRISTALIX_MONITORING_PROJECT_KEY | \
    CRISTALIX_RESERVE_PROJECT_TOKEN | \
    CRISTALIX_RESERVE_PROJECT_KEY)
        ;;
    *)
        echo "Unsupported external production variable: $KEY" >&2
        exit 1
        ;;
esac

if [ ! -f "$TARGET_ENV" ]; then
    echo "Production environment does not exist: $TARGET_ENV" >&2
    exit 1
fi

if [ ! -r /dev/tty ]; then
    echo "A terminal is required. Run this command directly through SSH." >&2
    exit 1
fi

restore_terminal() {
    stty echo </dev/tty 2>/dev/null || true
}

trap restore_terminal EXIT INT TERM

printf 'Enter %s: ' "$KEY" >/dev/tty
stty -echo </dev/tty
IFS= read -r VALUE </dev/tty
stty echo </dev/tty
printf '\n' >/dev/tty

if [ -z "$VALUE" ]; then
    echo "$KEY must not be empty" >&2
    exit 1
fi

TEMP_FILE="$(mktemp)"
trap 'rm -f "$TEMP_FILE"; restore_terminal' EXIT INT TERM

awk -v key="$KEY" -v value="$VALUE" '
    BEGIN { found = 0 }
    index($0, key "=") == 1 {
        if (!found) {
            print key "=" value
            found = 1
        }
        next
    }
    { print }
    END {
        if (!found) {
            print key "=" value
        }
    }
' "$TARGET_ENV" > "$TEMP_FILE"

install -o root -g root -m 0600 "$TEMP_FILE" "$TARGET_ENV"

echo "$KEY updated without printing its value"
