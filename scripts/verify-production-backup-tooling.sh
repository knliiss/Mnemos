#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup-production.sh"
OFFSITE_SCRIPT="$SCRIPT_DIR/backup-production-offsite.sh"
PRUNE_SCRIPT="$SCRIPT_DIR/prune-production-local-backups.sh"
RUN_SCRIPT="$SCRIPT_DIR/run-production-backup.sh"
HEALTH_SCRIPT="$SCRIPT_DIR/check-production-health.sh"
RESTORE_CORE_SCRIPT="$SCRIPT_DIR/restore-production-core.sh"
RESTORE_REDIS_SCRIPT="$SCRIPT_DIR/restore-production-redis.sh"
RUNTIME_INSTALLER="$SCRIPT_DIR/install-production-runtime.sh"

fail() {
    echo "$1" >&2
    exit 1
}

for script in \
    "$BACKUP_SCRIPT" \
    "$OFFSITE_SCRIPT" \
    "$PRUNE_SCRIPT" \
    "$RUN_SCRIPT" \
    "$HEALTH_SCRIPT" \
    "$RESTORE_CORE_SCRIPT" \
    "$RESTORE_REDIS_SCRIPT" \
    "$RUNTIME_INSTALLER"; do
    [ -f "$script" ] || fail "Production backup/runtime script is missing: $script"
done

grep -Fq 'secrets_included=false' "$BACKUP_SCRIPT" ||
    fail "Local production snapshots must explicitly exclude secrets"

if grep -Eq 'cp .*\/etc\/mnemos|tar .*\/etc\/mnemos' "$BACKUP_SCRIPT"; then
    fail "Local production snapshots must not copy /etc/mnemos secrets"
fi

grep -Fq 'latest-local' "$BACKUP_SCRIPT" ||
    fail "Local backup creation must only publish locally verified state"

grep -Fq '/etc/mnemos' "$OFFSITE_SCRIPT" ||
    fail "Encrypted off-site backup must include /etc/mnemos"

grep -Fq 'RESTIC_PASSWORD_FILE' "$OFFSITE_SCRIPT" ||
    fail "Off-site backup must use a Restic password file"

grep -Fq 'root:root' "$OFFSITE_SCRIPT" ||
    fail "Off-site backup configuration ownership must be validated"

grep -Fq 'MNEMOS_LOCAL_BACKUP_KEEP_COUNT' "$PRUNE_SCRIPT" ||
    fail "Local backup pruning must have an explicit retention count"

grep -Fq "grep -E '^[0-9]{8}T[0-9]{6}Z$'" "$PRUNE_SCRIPT" ||
    fail "Local backup pruning must only target timestamp backup directories"

grep -Fq 'backup-production-offsite.sh' "$RUN_SCRIPT" ||
    fail "Backup pipeline must upload off-site before completion"

grep -Fq 'prune-production-local-backups.sh' "$RUN_SCRIPT" ||
    fail "Backup pipeline must prune local snapshots after off-site backup"

grep -Fq 'latest-successful' "$RUN_SCRIPT" ||
    fail "Backup pipeline must publish a fully successful completion marker"

grep -Fq 'latest-successful' "$HEALTH_SCRIPT" ||
    fail "Production monitoring must use the fully successful backup marker"

offsite_line="$(grep -n 'backup-production-offsite.sh' "$RUN_SCRIPT" | head -n 1 | cut -d: -f1)"
prune_line="$(grep -n 'prune-production-local-backups.sh' "$RUN_SCRIPT" | head -n 1 | cut -d: -f1)"
success_line="$(grep -n 'mv -f "$SUCCESSFUL_LATEST_TEMP" "$SUCCESSFUL_LATEST_FILE"' "$RUN_SCRIPT" | head -n 1 | cut -d: -f1)"

if [ "$prune_line" -le "$offsite_line" ]; then
    fail "Local backup pruning must run only after successful off-site upload"
fi

if [ -z "$success_line" ] || [ "$success_line" -le "$prune_line" ]; then
    fail "Successful backup state must only publish after off-site backup and local pruning succeed"
fi

grep -Fq 'MNEMOS_RESTORE_CONFIRM' "$RESTORE_CORE_SCRIPT" ||
    fail "Core restore must require exact confirmation"

grep -Fq 'docker stack services -q' "$RESTORE_CORE_SCRIPT" ||
    fail "Core restore must reject a running application stack"

grep -Fq 'stop keycloak' "$RESTORE_CORE_SCRIPT" ||
    fail "Keycloak must be stopped before restoring its database"

grep -Fq 'MNEMOS_RESTORE_REDIS_CONFIRM' "$RESTORE_REDIS_SCRIPT" ||
    fail "Redis restore must require exact confirmation"

grep -Fq 'docker stack services -q' "$RESTORE_REDIS_SCRIPT" ||
    fail "Redis restore must reject a running application stack"

grep -Fq '/opt/mnemos/runtime' "$RUNTIME_INSTALLER" ||
    fail "Production runtime tooling must be installed outside the Actions workspace"

grep -Fq 'mv -Tf' "$RUNTIME_INSTALLER" ||
    fail "Production runtime current symlink must switch atomically"

echo "Production backup, restore and runtime safety boundaries verified"
