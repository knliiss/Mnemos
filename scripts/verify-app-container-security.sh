#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
SECURITY_COMPOSE="$REPOSITORY_ROOT/infrastructure/compose/compose.app-security.yml"

fail() {
    echo "$1" >&2
    exit 1
}

require_text() {
    text="$1"
    message="$2"

    grep -Fq "$text" "$SECURITY_COMPOSE" || fail "$message"
}

[ -f "$SECURITY_COMPOSE" ] || fail "Application container security compose file is missing"

require_text 'user: "10001:10001"' "Application containers must run as a non-root UID"
require_text 'read_only: true' "Application containers must use a read-only root filesystem"
require_text 'cap_drop:' "Application containers must drop Linux capabilities"
require_text '    - ALL' "Application containers must drop all Linux capabilities"
require_text 'no-new-privileges:true' "Application containers must disable privilege escalation"
require_text '/tmp:rw,noexec,nosuid,size=64m' "Application containers must use a bounded hardened tmpfs"

verified_services=0

for compose_file in "$REPOSITORY_ROOT"/infrastructure/compose/apps/compose.*-service.yml; do
    [ -f "$compose_file" ] || continue

    service="$(basename "$compose_file" .yml)"
    service="${service#compose.}"

    if ! grep -Eq "^  ${service}:$" "$SECURITY_COMPOSE"; then
        fail "$service is missing from application container hardening"
    fi

    if grep -Eq '^[[:space:]]*privileged:[[:space:]]*true([[:space:]]|$)' "$compose_file"; then
        fail "$service must not run privileged"
    fi

    verified_services=$((verified_services + 1))
done

if [ "$verified_services" -eq 0 ]; then
    fail "No application service compose files were verified"
fi

echo "Application container security verified: $verified_services services"
