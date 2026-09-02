#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

fail() {
    echo "$1" >&2
    exit 1
}

verified_configs=0

for config_file in "$REPOSITORY_ROOT"/apps/*-service/src/main/resources/application.properties; do
    [ -f "$config_file" ] || continue

    relative_path="${config_file#"$REPOSITORY_ROOT/"}"

    if grep -Eq '\$\{APP_POSTGRES_USER:[^}]+' "$config_file"; then
        fail "Application config contains a PostgreSQL username fallback: $relative_path"
    fi

    if grep -Eq '\$\{[A-Z0-9_]*(PASSWORD|SECRET|TOKEN|API_KEY|PRIVATE_KEY)[A-Z0-9_]*:[^}]+' "$config_file"; then
        fail "Application config contains a non-empty credential fallback: $relative_path"
    fi

    verified_configs=$((verified_configs + 1))
done

if [ "$verified_configs" -eq 0 ]; then
    fail "No application property files were verified"
fi

verified_compose_files=0

for compose_file in \
    "$REPOSITORY_ROOT"/infrastructure/compose/*.yml \
    "$REPOSITORY_ROOT"/infrastructure/compose/apps/*.yml \
    "$REPOSITORY_ROOT"/infrastructure/production/*.yml; do
    [ -f "$compose_file" ] || continue

    relative_path="${compose_file#"$REPOSITORY_ROOT/"}"

    if grep -Eq '\$\{[A-Z0-9_]*(PASSWORD|SECRET|TOKEN|API_KEY|PRIVATE_KEY)[A-Z0-9_]*:-[^}]+' "$compose_file"; then
        fail "Compose config contains a non-empty credential fallback: $relative_path"
    fi

    verified_compose_files=$((verified_compose_files + 1))
done

if [ "$verified_compose_files" -eq 0 ]; then
    fail "No Compose files were verified"
fi

if grep -R -n -E 'mnemos_local_(password|redis_password|storage_password)' \
    "$REPOSITORY_ROOT/apps" \
    "$REPOSITORY_ROOT/infrastructure/compose" \
    "$REPOSITORY_ROOT/infrastructure/production" >/dev/null 2>&1; then
    grep -R -n -E 'mnemos_local_(password|redis_password|storage_password)' \
        "$REPOSITORY_ROOT/apps" \
        "$REPOSITORY_ROOT/infrastructure/compose" \
        "$REPOSITORY_ROOT/infrastructure/production" >&2 || true

    fail "Application or Compose sources contain legacy local credential defaults"
fi

echo "Credential defaults verified: $verified_configs application configs, $verified_compose_files Compose files"
