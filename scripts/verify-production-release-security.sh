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

uses_production_runner() {
    workflow_path="$1"

    grep -Eq \
        '^[[:space:]]*-[[:space:]]*mnemos-production[[:space:]]*$|^[[:space:]]*runs-on:[[:space:]].*mnemos-production' \
        "$workflow_path"
}

[ -f "$WORKFLOW" ] || fail "Production image batch workflow is missing"
[ -f "$KEYCLOAK_CLIENTS" ] || fail "Keycloak client configuration is missing"

WORKFLOW_CONTENT="$(cat "$WORKFLOW")"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'workflow_dispatch:' ||
    fail "Production image batch must be manually dispatched"

if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Eq '^[[:space:]]+(push|pull_request|schedule|workflow_run):'; then
    fail "Production image batch must not have automatic triggers"
fi

if uses_production_runner "$WORKFLOW"; then
    fail "Production image batch must not target the production runner"
fi

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'runs-on: [self-hosted, Linux, X64, mnemos-backend]' ||
    fail "Production image publishing must use the hardened backend runner"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'packages: write' ||
    fail "Production image publishing must explicitly own package write permission"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'source_sha:' ||
    fail "Production image batch must require an explicit immutable source SHA"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq '^[0-9a-f]{40}$' ||
    fail "Production image batch must validate the full source SHA"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'ref: ${{ inputs.source_sha }}' ||
    fail "Production image batch must checkout the explicitly requested revision"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'CHECKED_OUT_SHA="$(git rev-parse HEAD)"' ||
    fail "Production image batch must verify the checked-out revision"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'MNEMOS_IMAGE_TAG: ${{ inputs.source_sha }}' ||
    fail "Production image batch must tag images with the immutable source SHA"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'sh scripts/publish-production-images.sh' ||
    fail "Production image batch must use the catalog-backed publisher"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'DOCKER_CONFIG: ${{ runner.temp }}/mnemos-production-image-batch-docker-auth' ||
    fail "Production image publishing credentials must use an isolated Docker config"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'rm -rf "$DOCKER_CONFIG"' ||
    fail "Production image publishing credentials must be removed after publishing"

for forbidden in \
    'docker service update' \
    'docker stack deploy' \
    'scripts/start-production-infra.sh' \
    'scripts/deploy-production-apps.sh' \
    'production_deployment_executor.py --execute' \
    '/opt/mnemos/current' \
    '/etc/mnemos'; do
    if printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq "$forbidden"; then
        fail "Production image batch contains runtime mutation capability: $forbidden"
    fi
done

if grep -Fq 'https://app.knalis.rest' "$KEYCLOAK_CLIENTS"; then
    fail "Keycloak client origins must be parameterized instead of hardcoded"
fi

grep -Fq '$(env:MNEMOS_FRONTEND_ORIGIN)' "$KEYCLOAK_CLIENTS" ||
    fail "Keycloak client configuration must use MNEMOS_FRONTEND_ORIGIN"

echo "Production image batch and Keycloak origin boundaries verified"
