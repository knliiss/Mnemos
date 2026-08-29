#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-service-image.yml"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$WORKFLOW" ] || fail "Production Service Image workflow is missing"

WORKFLOW_CONTENT="$(cat "$WORKFLOW")"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '  workflow_dispatch:' ||
    fail "Production Service Image must be manually dispatched"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "if: github.ref == 'refs/heads/master'" ||
    fail "Production Service Image must only run from the master workflow revision"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      - mnemos-backend' ||
    fail "Production Service Image must use the hardened backend runner"

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq 'runs-on:.*mnemos-production|^[[:space:]]*-[[:space:]]*mnemos-production$'; then
    fail "Production Service Image must never target the production runner"
fi

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '  contents: read' ||
    fail "Production Service Image must keep workflow-level contents read-only"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      packages: write' ||
    fail "Production Service Image publish job must explicitly own package write permission"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'persist-credentials: false' ||
    fail "Production Service Image checkout must not persist Git credentials"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'python3 scripts/publish-production-service-image.py' ||
    fail "Production Service Image must use the shared single-service publisher"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '        if: always()' ||
    fail "Production Service Image credential cleanup must run after failures"

for forbidden in \
    'sudo ' \
    'docker service' \
    'docker stack' \
    'docker compose' \
    'docker restart' \
    'docker run' \
    'docker exec'; do
    if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "$forbidden"; then
        fail "Production Service Image contains forbidden runtime capability: $forbidden"
    fi
done

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq '^[[:space:]]*(push|pull_request|workflow_run):'; then
    fail "Production Service Image must not have an automatic trigger"
fi

echo "Production service image workflow security boundary verified"
