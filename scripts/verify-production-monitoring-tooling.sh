#!/usr/bin/env sh

set -eu

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
HEALTH_SCRIPT="$REPOSITORY_ROOT/scripts/check-production-health.sh"
INSTALL_SCRIPT="$REPOSITORY_ROOT/scripts/install-production-monitoring.sh"
SERVICE_UNIT="$REPOSITORY_ROOT/infrastructure/systemd/mnemos-production-health.service"
TIMER_UNIT="$REPOSITORY_ROOT/infrastructure/systemd/mnemos-production-health.timer"

fail() {
    echo "Production monitoring verification failed: $*" >&2
    exit 1
}

sh -n "$HEALTH_SCRIPT"
sh -n "$INSTALL_SCRIPT"

for file in "$SERVICE_UNIT" "$TIMER_UNIT"; do
    [ -f "$file" ] || fail "missing $file"
done

grep -Fq 'ExecStart=/opt/mnemos/current/scripts/check-production-health.sh' "$SERVICE_UNIT" \
    || fail "health service does not execute the immutable runtime health script"

grep -Fq 'NoNewPrivileges=true' "$SERVICE_UNIT" \
    || fail "health service is missing NoNewPrivileges"

grep -Fq 'ProtectSystem=strict' "$SERVICE_UNIT" \
    || fail "health service is missing ProtectSystem=strict"

grep -Fq 'ProtectKernelTunables=true' "$SERVICE_UNIT" \
    || fail "health service must protect kernel tunables"

grep -Fq 'ProtectKernelModules=true' "$SERVICE_UNIT" \
    || fail "health service must protect kernel modules"

grep -Fq 'ProtectControlGroups=true' "$SERVICE_UNIT" \
    || fail "health service must protect control groups"

grep -Fq 'RestrictSUIDSGID=true' "$SERVICE_UNIT" \
    || fail "health service must restrict SUID/SGID transitions"

grep -Fq 'LockPersonality=true' "$SERVICE_UNIT" \
    || fail "health service must lock execution personality"

grep -Fq 'RestrictRealtime=true' "$SERVICE_UNIT" \
    || fail "health service must restrict realtime scheduling"

grep -Fq 'OnUnitActiveSec=5m' "$TIMER_UNIT" \
    || fail "health timer must run every five minutes"

grep -Fq 'Persistent=true' "$TIMER_UNIT" \
    || fail "health timer must be persistent"

if grep -Eq 'systemctl[[:space:]]+(enable|start|restart)|docker[[:space:]]+stack[[:space:]]+deploy' "$HEALTH_SCRIPT"; then
    fail "health check must be observation-only"
fi

grep -Fq 'MNEMOS_ENABLE_PRODUCTION_MONITORING:-false' "$INSTALL_SCRIPT" \
    || fail "monitoring installer must default to disabled"

grep -Fq 'systemctl enable --now mnemos-production-health.timer' "$INSTALL_SCRIPT" \
    || fail "monitoring installer does not have an explicit enable path"

grep -Fq "docker node inspect self" "$HEALTH_SCRIPT" \
    || fail "health check does not verify Swarm node state"

grep -Fq "docker stack services" "$HEALTH_SCRIPT" \
    || fail "health check does not verify replica convergence"

grep -Fq 'MNEMOS_MONITOR_INODE_USAGE_LIMIT_PERCENT:-90' "$HEALTH_SCRIPT" \
    || fail "health check does not enforce an inode pressure threshold"

grep -Fq 'df -Pi /' "$HEALTH_SCRIPT" \
    || fail "health check does not inspect root filesystem inode usage"

grep -Fq 'production_runtime_state.py' "$HEALTH_SCRIPT" \
    || fail "health check does not verify structured production runtime state"

grep -Fq 'current-state.json' "$HEALTH_SCRIPT" \
    || fail "health check does not use the per-service deployment state when available"

grep -Fq 'verify-production-infra.sh' "$HEALTH_SCRIPT" \
    || fail "health check does not verify stateful production infrastructure after deployment"

grep -Fq 'systemctl is-failed --quiet mnemos-production-backup.service' "$HEALTH_SCRIPT" \
    || fail "health check does not fail immediately after a failed scheduled backup"

grep -Fq "mnemos-production-backup.timer" "$HEALTH_SCRIPT" \
    || fail "health check does not verify backup freshness when backups are enabled"

echo "Production monitoring tooling verified"
