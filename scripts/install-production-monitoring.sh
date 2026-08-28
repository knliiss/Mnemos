#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
SERVICE_SOURCE="$REPOSITORY_ROOT/infrastructure/systemd/mnemos-production-health.service"
TIMER_SOURCE="$REPOSITORY_ROOT/infrastructure/systemd/mnemos-production-health.timer"
SERVICE_TARGET="/etc/systemd/system/mnemos-production-health.service"
TIMER_TARGET="/etc/systemd/system/mnemos-production-health.timer"
ENABLE="${MNEMOS_ENABLE_PRODUCTION_MONITORING:-false}"

for file in "$SERVICE_SOURCE" "$TIMER_SOURCE"; do
    if [ ! -f "$file" ]; then
        echo "Required monitoring unit is missing: $file" >&2
        exit 1
    fi
done

install -o root -g root -m 0644 "$SERVICE_SOURCE" "$SERVICE_TARGET"
install -o root -g root -m 0644 "$TIMER_SOURCE" "$TIMER_TARGET"

systemctl daemon-reload

if [ "$ENABLE" = "true" ]; then
    systemctl enable --now mnemos-production-health.timer
    systemctl start mnemos-production-health.service
else
    echo "Production monitoring units installed but not enabled."
    echo "Set MNEMOS_ENABLE_PRODUCTION_MONITORING=true to enable the timer explicitly."
fi
