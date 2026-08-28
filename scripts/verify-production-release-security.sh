#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-images.yml"
KEYCLOAK_CLIENTS="$REPOSITORY_ROOT/infrastructure/keycloak/config/40-clients.yaml"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$WORKFLOW" ] || fail "Production Release workflow is missing"
[ -f "$KEYCLOAK_CLIENTS" ] || fail "Keycloak client configuration is missing"

WORKFLOW_CONTENT="$(cat "$WORKFLOW")"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'workflow_run:' ||
    fail "Production Release must be triggered from a completed verification workflow"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      - Backend Verify' ||
    fail "Production Release must depend on Backend Verify"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      - Stack Smoke' ||
    fail "Production Release must depend on Stack Smoke"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "github.event.workflow_run.conclusion == 'success'" ||
    fail "Production Release must require a successful triggering verification workflow"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "github.event.workflow_run.event == 'push'" ||
    fail "Production Release must reject pull request verification runs"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "github.event.workflow_run.head_branch == 'master'" ||
    fail "Production Release must only accept master revisions"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'github.event.workflow_run.head_repository.full_name == github.repository' ||
    fail "Production Release must reject revisions from another repository"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'PEER_WORKFLOW="Stack Smoke"' ||
    fail "Production Release must require Stack Smoke when Backend Verify triggers it"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'PEER_WORKFLOW="Backend Verify"' ||
    fail "Production Release must require Backend Verify when Stack Smoke triggers it"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'vars.MNEMOS_PRODUCTION_DEPLOY_ENABLED == '\''true'\''' ||
    fail "Production deployment must remain explicitly gated"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      - mnemos-production' ||
    fail "Production deploy must target the dedicated production runner"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '    runs-on: ubuntu-latest' ||
    fail "Production image publishing must use a GitHub-hosted runner"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      packages: write' ||
    fail "Production image publishing must explicitly own package write permission"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      packages: read' ||
    fail "Production deploy must have package read permission only"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'ref: ${{ github.event.workflow_run.head_sha }}' ||
    fail "Production jobs must checkout the verified immutable revision"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '      group: mnemos-production-deploy' ||
    fail "Production deploys must be globally serialized across release SHAs"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'DOCKER_CONFIG=/run/mnemos-production-docker-auth' ||
    fail "Production registry credentials must use an isolated Docker config"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '          sudo -n rm -rf /run/mnemos-production-docker-auth' ||
    fail "Production registry credentials must be removed after the deploy job"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '        if: always()' ||
    fail "Production registry credential cleanup must run after failures"

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq '^[[:space:]]*pull_request:'; then
    fail "Production Release must never be directly triggerable by pull requests"
fi

if grep -Fq 'https://app.knalis.rest' "$KEYCLOAK_CLIENTS"; then
    fail "Keycloak client origins must be parameterized instead of hardcoded"
fi

grep -Fq '$(env:MNEMOS_FRONTEND_ORIGIN)' "$KEYCLOAK_CLIENTS" ||
    fail "Keycloak client configuration must use MNEMOS_FRONTEND_ORIGIN"

echo "Production release workflow and Keycloak origin boundaries verified"
