#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-hotfix-plan.yml"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$WORKFLOW" ] || fail "Production Hotfix Plan workflow is missing"

WORKFLOW_CONTENT="$(cat "$WORKFLOW")"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '  workflow_dispatch:' ||
    fail "Production Hotfix Plan must be manually dispatched"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '  contents: read' ||
    fail "Production Hotfix Plan must keep repository contents read-only"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'runs-on: [self-hosted, Linux, X64, mnemos-backend]' ||
    fail "Production Hotfix Plan must use the hardened backend runner"

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq 'runs-on:.*mnemos-production'; then
    fail "Production Hotfix Plan must never target the production runner"
fi

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'persist-credentials: false' ||
    fail "Production Hotfix Plan checkout must not persist Git credentials"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'ref: ${{ github.sha }}' ||
    fail "Production Hotfix Plan must pin tooling to the workflow run head SHA"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'PLANNING_HEAD_SHA: ${{ github.sha }}' ||
    fail "Production Hotfix Plan must explicitly verify the workflow run head SHA"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'git merge-base --is-ancestor "$PLANNING_HEAD_SHA" origin/master' ||
    fail "Production Hotfix Plan tooling must be reachable from trusted master history"

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq '^[[:space:]]+ref:[[:space:]]+master[[:space:]]*$'; then
    fail "Production Hotfix Plan must not checkout the moving master ref"
fi

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'python3 scripts/production_hotfix_plan.py' ||
    fail "Production Hotfix Plan must use the shared scoped planner"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'actions/upload-artifact@' ||
    fail "Production Hotfix Plan must publish its immutable plan bundle as an artifact"

for forbidden in \
    'packages: write' \
    'actions: write' \
    'deployments: write' \
    'statuses: write' \
    'secrets.' \
    'sudo ' \
    'docker service' \
    'docker stack' \
    'docker compose' \
    'docker restart' \
    'docker run' \
    'docker exec'; do
    if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "$forbidden"; then
        fail "Production Hotfix Plan contains forbidden runtime capability: $forbidden"
    fi
done

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq '^[[:space:]]*(push|pull_request|workflow_run):'; then
    fail "Production Hotfix Plan must not have an automatic trigger"
fi

echo "Production hotfix plan workflow security boundary verified"
