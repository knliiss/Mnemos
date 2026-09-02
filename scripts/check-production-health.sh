#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

STACK_NAME="${MNEMOS_SWARM_STACK_NAME:-mnemos}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-mnemos}"
RUNTIME_ROOT="${MNEMOS_RUNTIME_CURRENT_LINK:-/opt/mnemos/current}"
DISK_USAGE_LIMIT_PERCENT="${MNEMOS_MONITOR_DISK_USAGE_LIMIT_PERCENT:-90}"
INODE_USAGE_LIMIT_PERCENT="${MNEMOS_MONITOR_INODE_USAGE_LIMIT_PERCENT:-90}"
MEMORY_AVAILABLE_MIN_PERCENT="${MNEMOS_MONITOR_MEMORY_AVAILABLE_MIN_PERCENT:-10}"
BACKUP_MAX_AGE_SECONDS="${MNEMOS_MONITOR_BACKUP_MAX_AGE_SECONDS:-129600}"
BACKUP_SUCCESSFUL_LATEST_FILE="${MNEMOS_BACKUP_STATE_DIR:-/var/lib/mnemos-backup}/latest-successful"
DEPLOY_STATE_DIR="${MNEMOS_DEPLOY_STATE_DIR:-/var/lib/mnemos-deploy}"
DEPLOY_RELEASE_FILE="$DEPLOY_STATE_DIR/current-release"
DEPLOY_STRUCTURED_STATE_FILE="$DEPLOY_STATE_DIR/current-state.json"
VERIFY_INFRA_SCRIPT="$RUNTIME_ROOT/scripts/verify-production-infra.sh"
VERIFY_RUNTIME_STATE_SCRIPT="$RUNTIME_ROOT/scripts/production_runtime_state.py"

fail() {
    echo "Production health check failed: $*" >&2
    exit 1
}

require_integer() {
    name="$1"
    value="$2"

    case "$value" in
        ''|*[!0-9]*)
            fail "$name must be an integer"
            ;;
    esac
}

require_integer "MNEMOS_MONITOR_DISK_USAGE_LIMIT_PERCENT" "$DISK_USAGE_LIMIT_PERCENT"
require_integer "MNEMOS_MONITOR_INODE_USAGE_LIMIT_PERCENT" "$INODE_USAGE_LIMIT_PERCENT"
require_integer "MNEMOS_MONITOR_MEMORY_AVAILABLE_MIN_PERCENT" "$MEMORY_AVAILABLE_MIN_PERCENT"
require_integer "MNEMOS_MONITOR_BACKUP_MAX_AGE_SECONDS" "$BACKUP_MAX_AGE_SECONDS"

swarm_state="$(docker info --format '{{.Swarm.LocalNodeState}}')"

if [ "$swarm_state" != "active" ]; then
    fail "Docker Swarm state is $swarm_state"
fi

node_status="$(docker node inspect self --format '{{.Status.State}}')"
node_availability="$(docker node inspect self --format '{{.Spec.Availability}}')"

if [ "$node_status" != "ready" ]; then
    fail "Swarm node status is $node_status"
fi

if [ "$node_availability" != "active" ]; then
    fail "Swarm node availability is $node_availability"
fi

disk_usage="$(df -P / | awk 'NR == 2 { gsub(/%/, "", $5); print $5 }')"
require_integer "root filesystem usage" "$disk_usage"

if [ "$disk_usage" -ge "$DISK_USAGE_LIMIT_PERCENT" ]; then
    fail "root filesystem usage is ${disk_usage}%"
fi

inode_usage="$(df -Pi / | awk 'NR == 2 { gsub(/%/, "", $5); print $5 }')"
require_integer "root filesystem inode usage" "$inode_usage"

if [ "$inode_usage" -ge "$INODE_USAGE_LIMIT_PERCENT" ]; then
    fail "root filesystem inode usage is ${inode_usage}%"
fi

memory_total_kib="$(awk '/^MemTotal:/ { print $2 }' /proc/meminfo)"
memory_available_kib="$(awk '/^MemAvailable:/ { print $2 }' /proc/meminfo)"

require_integer "MemTotal" "$memory_total_kib"
require_integer "MemAvailable" "$memory_available_kib"

if [ "$memory_total_kib" -eq 0 ]; then
    fail "MemTotal is zero"
fi

memory_available_percent="$((memory_available_kib * 100 / memory_total_kib))"

if [ "$memory_available_percent" -lt "$MEMORY_AVAILABLE_MIN_PERCENT" ]; then
    fail "available memory is ${memory_available_percent}%"
fi

if docker stack ls --format '{{.Name}}' | grep -Fxq "$STACK_NAME"; then
    unhealthy_services="$(
        docker stack services "$STACK_NAME" \
            --format '{{.Name}} {{.Replicas}}' \
            | awk '
                {
                    split($2, replicas, "/")
                    if (replicas[1] != replicas[2]) {
                        print $0
                    }
                }
            '
    )"

    if [ -n "$unhealthy_services" ]; then
        echo "$unhealthy_services" >&2
        fail "one or more Swarm services have not converged"
    fi
elif [ -f "$DEPLOY_RELEASE_FILE" ] || [ -f "$DEPLOY_STRUCTURED_STATE_FILE" ]; then
    fail "deployment state exists but Swarm stack $STACK_NAME is missing"
fi

if [ -f "$DEPLOY_STRUCTURED_STATE_FILE" ]; then
    if [ ! -f "$VERIFY_RUNTIME_STATE_SCRIPT" ]; then
        fail "production runtime state verifier is missing: $VERIFY_RUNTIME_STATE_SCRIPT"
    fi

    if ! python3 "$VERIFY_RUNTIME_STATE_SCRIPT" \
        verify \
        --state "$DEPLOY_STRUCTURED_STATE_FILE"; then
        fail "production runtime images drifted from the recorded deployment state"
    fi
fi

if [ -f "$DEPLOY_RELEASE_FILE" ] || [ -f "$DEPLOY_STRUCTURED_STATE_FILE" ]; then
    if [ ! -f "$VERIFY_INFRA_SCRIPT" ]; then
        fail "production infrastructure verifier is missing: $VERIFY_INFRA_SCRIPT"
    fi

    if ! COMPOSE_PROJECT_NAME="$COMPOSE_PROJECT_NAME" sh "$VERIFY_INFRA_SCRIPT"; then
        fail "production infrastructure containers are not healthy"
    fi
fi

if systemctl is-enabled --quiet mnemos-production-backup.timer 2>/dev/null; then
    if systemctl is-failed --quiet mnemos-production-backup.service 2>/dev/null; then
        fail "latest scheduled production backup service execution failed"
    fi

    if [ ! -s "$BACKUP_SUCCESSFUL_LATEST_FILE" ]; then
        fail "backup timer is enabled but no fully successful backup pipeline state exists"
    fi

    latest_backup="$(cat "$BACKUP_SUCCESSFUL_LATEST_FILE")"

    if [ ! -d "$latest_backup" ]; then
        fail "latest successful backup directory does not exist: $latest_backup"
    fi

    backup_mtime="$(stat -c '%Y' "$latest_backup")"
    now="$(date +%s)"
    backup_age="$((now - backup_mtime))"

    if [ "$backup_age" -gt "$BACKUP_MAX_AGE_SECONDS" ]; then
        fail "latest successful backup pipeline is ${backup_age}s old"
    fi
fi

printf 'Production health check passed: disk=%s%% inodes=%s%% memory_available=%s%%\n' \
    "$disk_usage" \
    "$inode_usage" \
    "$memory_available_percent"
