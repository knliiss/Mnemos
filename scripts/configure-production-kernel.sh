#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

SYSCTL_FILE="${MNEMOS_REDIS_SYSCTL_FILE:-/etc/sysctl.d/99-mnemos-redis.conf}"
SYSCTL_DIR="$(dirname -- "$SYSCTL_FILE")"
TEMP_FILE="${SYSCTL_FILE}.tmp.$$"

cleanup() {
    rm -f "$TEMP_FILE"
}

trap cleanup EXIT INT TERM

install -d -o root -g root -m 0755 "$SYSCTL_DIR"
printf '%s\n' 'vm.overcommit_memory = 1' > "$TEMP_FILE"
chmod 0644 "$TEMP_FILE"
chown root:root "$TEMP_FILE"
mv -f "$TEMP_FILE" "$SYSCTL_FILE"
trap - EXIT INT TERM

sysctl -w vm.overcommit_memory=1 >/dev/null

runtime_value="$(cat /proc/sys/vm/overcommit_memory)"
persistent_value="$(awk -F= '/^[[:space:]]*vm\.overcommit_memory[[:space:]]*=/{gsub(/[[:space:]]/, "", $2); print $2}' "$SYSCTL_FILE")"

if [ "$runtime_value" != 1 ] || [ "$persistent_value" != 1 ]; then
    echo "Redis kernel requirement was not applied correctly" >&2
    echo "runtime=$runtime_value persistent=$persistent_value" >&2
    exit 1
fi

printf 'Configured Redis kernel requirement: vm.overcommit_memory=1\n'
