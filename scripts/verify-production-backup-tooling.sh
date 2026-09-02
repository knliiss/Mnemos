#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
BACKUP_SCRIPT="$SCRIPT_DIR/backup-production.sh"
OFFSITE_SCRIPT="$SCRIPT_DIR/backup-production-offsite.sh"
PRUNE_SCRIPT="$SCRIPT_DIR/prune-production-local-backups.sh"
RUN_SCRIPT="$SCRIPT_DIR/run-production-backup.sh"
ENABLE_SCRIPT="$SCRIPT_DIR/enable-production-backups.sh"
HEALTH_SCRIPT="$SCRIPT_DIR/check-production-health.sh"
RESTORE_CORE_SCRIPT="$SCRIPT_DIR/restore-production-core.sh"
RESTORE_REDIS_SCRIPT="$SCRIPT_DIR/restore-production-redis.sh"
RUNTIME_INSTALLER="$SCRIPT_DIR/install-production-runtime.sh"
BACKUP_SERVICE="$SCRIPT_DIR/../infrastructure/systemd/mnemos-production-backup.service"

fail() {
    echo "$1" >&2
    exit 1
}

for script in \
    "$BACKUP_SCRIPT" \
    "$OFFSITE_SCRIPT" \
    "$PRUNE_SCRIPT" \
    "$RUN_SCRIPT" \
    "$ENABLE_SCRIPT" \
    "$HEALTH_SCRIPT" \
    "$RESTORE_CORE_SCRIPT" \
    "$RESTORE_REDIS_SCRIPT" \
    "$RUNTIME_INSTALLER"; do
    [ -f "$script" ] || fail "Production backup/runtime script is missing: $script"
done

[ -f "$BACKUP_SERVICE" ] || fail "Production backup systemd service is missing"

grep -Fq 'secrets_included=false' "$BACKUP_SCRIPT" ||
    fail "Local production snapshots must explicitly exclude secrets"

if grep -Eq 'cp .*\/etc\/mnemos|tar .*\/etc\/mnemos' "$BACKUP_SCRIPT"; then
    fail "Local production snapshots must not copy /etc/mnemos secrets"
fi

grep -Fq 'latest-local' "$BACKUP_SCRIPT" ||
    fail "Local backup creation must only publish locally verified state"

pg_restore_list_count="$(grep -Fc 'pg_restore --list' "$BACKUP_SCRIPT")"

if [ "$pg_restore_list_count" -ne 2 ]; then
    fail "Both PostgreSQL production dumps must be structurally validated at creation time"
fi

grep -Fq '< "$BACKUP_DIR/app-postgres.dump"' "$BACKUP_SCRIPT" ||
    fail "Application PostgreSQL dump must be validated through pg_restore"

grep -Fq '< "$BACKUP_DIR/keycloak-postgres.dump"' "$BACKUP_SCRIPT" ||
    fail "Keycloak PostgreSQL dump must be validated through pg_restore"

grep -Fq '/etc/mnemos' "$OFFSITE_SCRIPT" ||
    fail "Encrypted off-site backup must include /etc/mnemos"

grep -Fq 'RESTIC_PASSWORD_FILE' "$OFFSITE_SCRIPT" ||
    fail "Off-site backup must use a Restic password file"

grep -Fq 'root:root' "$OFFSITE_SCRIPT" ||
    fail "Off-site backup configuration ownership must be validated"

grep -Fq 'MNEMOS_LOCAL_BACKUP_KEEP_COUNT:-7' "$PRUNE_SCRIPT" ||
    fail "Local backup pruning must retain seven daily snapshots by default"

grep -Fq "grep -E '^[0-9]{8}T[0-9]{6}Z$'" "$PRUNE_SCRIPT" ||
    fail "Local backup pruning must only target timestamp backup directories"

grep -Fq 'MNEMOS_BACKUP_MODE:-local' "$RUN_SCRIPT" ||
    fail "Production backup pipeline must default to local mode"

grep -Fq 'local | offsite' "$RUN_SCRIPT" ||
    fail "Production backup pipeline must explicitly allow only local or offsite modes"

grep -Fq 'if [ "$BACKUP_MODE" = "offsite" ]' "$RUN_SCRIPT" ||
    fail "Off-site upload must only run when offsite mode is explicitly selected"

grep -Fq 'backup-production-offsite.sh' "$RUN_SCRIPT" ||
    fail "Off-site mode must preserve encrypted Restic backup support"

grep -Fq 'prune-production-local-backups.sh' "$RUN_SCRIPT" ||
    fail "Backup pipeline must prune local snapshots before completion"

grep -Fq 'latest-successful' "$RUN_SCRIPT" ||
    fail "Backup pipeline must publish a fully successful completion marker"

grep -Fq 'latest-successful' "$HEALTH_SCRIPT" ||
    fail "Production monitoring must use the fully successful backup marker"

grep -Fq 'BACKUP_MODE="local"' "$ENABLE_SCRIPT" ||
    fail "Backup enablement must default to local-only operation"

grep -Fq 'offsite)' "$ENABLE_SCRIPT" ||
    fail "Backup enablement must retain an explicit offsite mode"

grep -Fq 'RESTIC_PASSWORD_FILE' "$ENABLE_SCRIPT" ||
    fail "Offsite backup enablement must require a Restic password file"

grep -Fq 'restic check' "$ENABLE_SCRIPT" ||
    fail "Offsite backup enablement must validate repository integrity before enabling the timer"

grep -Fq 'Environment=MNEMOS_BACKUP_MODE=local' "$BACKUP_SERVICE" ||
    fail "Scheduled production backups must default to local mode"

grep -Fq 'EnvironmentFile=-/etc/mnemos/backup.env' "$BACKUP_SERVICE" ||
    fail "Scheduled production backups must allow an explicit root-owned mode override"

prune_line="$(grep -n 'prune-production-local-backups.sh' "$RUN_SCRIPT" | head -n 1 | cut -d: -f1)"
success_line="$(grep -n 'mv -f "$SUCCESSFUL_LATEST_TEMP" "$SUCCESSFUL_LATEST_FILE"' "$RUN_SCRIPT" | head -n 1 | cut -d: -f1)"

if [ -z "$success_line" ] || [ "$success_line" -le "$prune_line" ]; then
    fail "Successful backup state must only publish after verification and local retention complete"
fi

grep -Fq 'MNEMOS_RESTORE_CONFIRM' "$RESTORE_CORE_SCRIPT" ||
    fail "Core restore must require exact confirmation"

grep -Fq 'docker stack services -q' "$RESTORE_CORE_SCRIPT" ||
    fail "Core restore must reject a running application stack"

grep -Fq 'stop keycloak' "$RESTORE_CORE_SCRIPT" ||
    fail "Keycloak must be stopped before restoring its database"

restore_transaction_count="$(grep -Fc -- '--single-transaction' "$RESTORE_CORE_SCRIPT")"

if [ "$restore_transaction_count" -ne 2 ]; then
    fail "Both PostgreSQL core restores must be atomic single-transaction restores"
fi

grep -Fq 'mc mirror --overwrite --remove --preserve' "$RESTORE_CORE_SCRIPT" ||
    fail "MinIO restore must remove objects that are absent from the selected backup"

grep -Fq 'MNEMOS_RESTORE_REDIS_CONFIRM' "$RESTORE_REDIS_SCRIPT" ||
    fail "Redis restore must require exact confirmation"

grep -Fq 'docker stack services -q' "$RESTORE_REDIS_SCRIPT" ||
    fail "Redis restore must reject a running application stack"

grep -Fq '/opt/mnemos/runtime' "$RUNTIME_INSTALLER" ||
    fail "Production runtime tooling must be installed outside the Actions workspace"

grep -Fq 'mv -Tf' "$RUNTIME_INSTALLER" ||
    fail "Production runtime current symlink must switch atomically"

echo "Production backup, restore and runtime safety boundaries verified"
