#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
RELEASE_TAG="${1:-${MNEMOS_RELEASE_TAG:-}}"
RUNTIME_ROOT="${MNEMOS_RUNTIME_ROOT:-/opt/mnemos/runtime}"
CURRENT_LINK="${MNEMOS_RUNTIME_CURRENT_LINK:-/opt/mnemos/current}"

if [ -z "$RELEASE_TAG" ]; then
    echo "Usage: $0 <release-tag>" >&2
    exit 1
fi

case "$RELEASE_TAG" in
    *[!0-9a-f]*)
        echo "Release tag must contain only lowercase hexadecimal characters" >&2
        exit 1
        ;;
esac

if [ "${#RELEASE_TAG}" -lt 12 ]; then
    echo "Release tag is too short" >&2
    exit 1
fi

TARGET="$RUNTIME_ROOT/$RELEASE_TAG"
TEMP_TARGET="$RUNTIME_ROOT/.${RELEASE_TAG}.tmp.$$"

install -d -o root -g root -m 0755 "$RUNTIME_ROOT"
rm -rf "$TEMP_TARGET"
install -d -o root -g root -m 0755 "$TEMP_TARGET"

cleanup() {
    rm -rf "$TEMP_TARGET"
}

trap cleanup EXIT INT TERM

cp -a "$REPOSITORY_ROOT/scripts" "$TEMP_TARGET/scripts"
cp -a "$REPOSITORY_ROOT/infrastructure" "$TEMP_TARGET/infrastructure"
cp -a "$REPOSITORY_ROOT/.env.example" "$TEMP_TARGET/.env.example"

find "$TEMP_TARGET" -type d -exec chmod 0755 {} +
find "$TEMP_TARGET" -type f -exec chmod 0644 {} +
find "$TEMP_TARGET/scripts" -type f -name '*.sh' -exec chmod 0755 {} +
find "$TEMP_TARGET/scripts" -type f -name '*.py' -exec chmod 0755 {} +
chown -R root:root "$TEMP_TARGET"

rm -rf "$TARGET"
mv "$TEMP_TARGET" "$TARGET"
trap - EXIT INT TERM

ln -sfn "$TARGET" "${CURRENT_LINK}.new"
mv -Tf "${CURRENT_LINK}.new" "$CURRENT_LINK"

printf 'Installed production runtime: %s\n' "$TARGET"
