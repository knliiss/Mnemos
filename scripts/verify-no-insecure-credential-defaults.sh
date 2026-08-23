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

    if grep -Eq '\$\{[A-Z0-9_]*(PASSWORD|SECRET|TOKEN|API_KEY)[A-Z0-9_]*:[^}]+' "$config_file"; then
        fail "Application config contains a non-empty credential fallback: $relative_path"
    fi

    verified_configs=$((verified_configs + 1))
done

if [ "$verified_configs" -eq 0 ]; then
    fail "No application property files were verified"
fi

if grep -R -n -E 'mnemos_local_(password|redis_password|storage_password)' \
    "$REPOSITORY_ROOT/apps" >/dev/null 2>&1; then
    grep -R -n -E 'mnemos_local_(password|redis_password|storage_password)' \
        "$REPOSITORY_ROOT/apps" >&2 || true

    fail "Application sources contain legacy local credential defaults"
fi

echo "Application credential defaults verified: $verified_configs service configs"
