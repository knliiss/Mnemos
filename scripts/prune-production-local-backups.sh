#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

BACKUP_ROOT="${MNEMOS_BACKUP_ROOT:-/var/backups/mnemos}"
KEEP_COUNT="${MNEMOS_LOCAL_BACKUP_KEEP_COUNT:-3}"

case "$KEEP_COUNT" in
    '' | *[!0-9]*)
        echo "MNEMOS_LOCAL_BACKUP_KEEP_COUNT must be a positive integer" >&2
        exit 1
        ;;
esac

if [ "$KEEP_COUNT" -lt 1 ]; then
    echo "MNEMOS_LOCAL_BACKUP_KEEP_COUNT must be at least 1" >&2
    exit 1
fi

if [ ! -d "$BACKUP_ROOT" ]; then
    exit 0
fi

candidate_file="$(mktemp)"

cleanup() {
    rm -f "$candidate_file"
}

trap cleanup EXIT INT TERM

find "$BACKUP_ROOT" \
    -mindepth 1 \
    -maxdepth 1 \
    -type d \
    -printf '%f\n' \
    | grep -E '^[0-9]{8}T[0-9]{6}Z$' \
    | sort -r \
    > "$candidate_file" || true

kept=0

while IFS= read -r backup_id; do
    [ -n "$backup_id" ] || continue

    kept=$((kept + 1))

    if [ "$kept" -le "$KEEP_COUNT" ]; then
        continue
    fi

    backup_dir="$BACKUP_ROOT/$backup_id"

    case "$backup_dir" in
        "$BACKUP_ROOT"/[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]T[0-9][0-9][0-9][0-9][0-9][0-9]Z)
            ;;
        *)
            echo "Refusing to prune unexpected path: $backup_dir" >&2
            exit 1
            ;;
    esac

    rm -rf --one-file-system "$backup_dir"
    printf 'Pruned local production backup: %s\n' "$backup_id"
done < "$candidate_file"
