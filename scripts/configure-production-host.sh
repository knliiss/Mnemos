#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
DOCKER_SETTINGS="$REPOSITORY_ROOT/infrastructure/production/docker-daemon-settings.json"
JOURNAL_SETTINGS="$REPOSITORY_ROOT/infrastructure/systemd/journald/90-mnemos-production.conf"
SNAPSHOT_ROOT="${MNEMOS_HOST_CONFIG_SNAPSHOT_ROOT:-/root}"
TIMESTAMP="$(date -u '+%Y%m%dT%H%M%SZ')"
SNAPSHOT_DIR="$SNAPSHOT_ROOT/mnemos-host-config-$TIMESTAMP"
TEMP_DAEMON="$(mktemp)"

cleanup() {
    rm -f "$TEMP_DAEMON"
}

trap cleanup EXIT INT TERM

for required_file in "$DOCKER_SETTINGS" "$JOURNAL_SETTINGS"; do
    if [ ! -f "$required_file" ]; then
        echo "Required host configuration file is missing: $required_file" >&2
        exit 1
    fi
done

install -d -o root -g root -m 0700 "$SNAPSHOT_DIR"

if [ -f /etc/docker/daemon.json ]; then
    cp -a /etc/docker/daemon.json "$SNAPSHOT_DIR/daemon.json"
fi

if [ -f /etc/systemd/journald.conf.d/90-mnemos-production.conf ]; then
    cp -a \
        /etc/systemd/journald.conf.d/90-mnemos-production.conf \
        "$SNAPSHOT_DIR/90-mnemos-production.conf"
fi

python3 - \
    /etc/docker/daemon.json \
    "$DOCKER_SETTINGS" \
    "$TEMP_DAEMON" <<'PY'
import json
import pathlib
import sys

current_path = pathlib.Path(sys.argv[1])
desired_path = pathlib.Path(sys.argv[2])
output_path = pathlib.Path(sys.argv[3])

current = {}

if current_path.exists():
    current = json.loads(current_path.read_text(encoding="utf-8"))

if not isinstance(current, dict):
    raise SystemExit("Existing Docker daemon configuration must be a JSON object")

desired = json.loads(desired_path.read_text(encoding="utf-8"))

if not isinstance(desired, dict):
    raise SystemExit("Desired Docker daemon settings must be a JSON object")

current.update(desired)
output_path.write_text(
    json.dumps(current, indent=2, sort_keys=True) + "\n",
    encoding="utf-8",
)
PY

dockerd --validate --config-file "$TEMP_DAEMON"

install -d -o root -g root -m 0755 /etc/docker
install -o root -g root -m 0644 "$TEMP_DAEMON" /etc/docker/daemon.json

install -d -o root -g root -m 0755 /etc/systemd/journald.conf.d
install \
    -o root \
    -g root \
    -m 0644 \
    "$JOURNAL_SETTINGS" \
    /etc/systemd/journald.conf.d/90-mnemos-production.conf

env DEBIAN_FRONTEND=noninteractive apt-get update
env DEBIAN_FRONTEND=noninteractive apt-get install -y restic

systemctl restart docker
systemctl restart systemd-journald

docker info --format 'LoggingDriver={{.LoggingDriver}} LiveRestore={{.LiveRestoreEnabled}}'
restic version

printf 'Production host configuration applied. Previous configuration snapshot: %s\n' "$SNAPSHOT_DIR"
