#!/usr/bin/env sh

set -eu

TOKEN_FILE="${MNEMOS_CLOUDFLARE_TUNNEL_TOKEN_FILE:-/etc/mnemos/cloudflare-tunnel.token}"
TOKEN_DIRECTORY="$(dirname -- "$TOKEN_FILE")"

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

if [ ! -r /dev/tty ] || [ ! -w /dev/tty ]; then
    echo "An interactive TTY is required" >&2
    exit 1
fi

restore_terminal() {
    stty echo < /dev/tty 2>/dev/null || true
}

trap restore_terminal EXIT INT TERM

printf 'Cloudflare Tunnel token: ' > /dev/tty
stty -echo < /dev/tty
IFS= read -r token < /dev/tty
restore_terminal
printf '\n' > /dev/tty

if [ -z "$token" ]; then
    echo "Cloudflare Tunnel token must not be empty" >&2
    exit 1
fi

case "$token" in
    *[!A-Za-z0-9._=-]*)
        echo "Cloudflare Tunnel token contains unexpected characters" >&2
        exit 1
        ;;
esac

install -d -o root -g root -m 0700 "$TOKEN_DIRECTORY"

umask 077
temporary_file="$(mktemp "${TOKEN_DIRECTORY}/.cloudflare-tunnel-token.XXXXXX")"

cleanup() {
    rm -f "$temporary_file"
}

trap 'restore_terminal; cleanup' EXIT INT TERM

printf '%s' "$token" > "$temporary_file"
install -o root -g root -m 0600 "$temporary_file" "$TOKEN_FILE"

printf 'Cloudflare Tunnel token stored in %s\n' "$TOKEN_FILE"
