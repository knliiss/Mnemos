#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-images.yml"
SERVICE_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-service-image.yml"
KEYCLOAK_CLIENTS="$REPOSITORY_ROOT/infrastructure/keycloak/config/40-clients.yaml"
START_INFRA_SCRIPT="$REPOSITORY_ROOT/scripts/start-production-infra.sh"
DEPLOY_APPS_SCRIPT="$REPOSITORY_ROOT/scripts/deploy-production-apps.sh"

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

verify_manual_image_workflow() {
    workflow_path="$1"
    workflow_name="$2"

    [ -f "$workflow_path" ] || fail "$workflow_name is missing"

    workflow_content="$(cat "$workflow_path")"

    printf '%s\n' "$workflow_content" | grep -Fq 'workflow_dispatch:' ||
        fail "$workflow_name must be manually dispatched"

    if printf '%s\n' "$workflow_content" | grep -Eq '^[[:space:]]+(push|pull_request|schedule|workflow_run):'; then
        fail "$workflow_name must not have automatic triggers"
    fi

    if uses_production_runner "$workflow_path"; then
        fail "$workflow_name must not target the production runner"
    fi

    printf '%s\n' "$workflow_content" | grep -Fq 'mnemos-backend' ||
        fail "$workflow_name must use the hardened backend runner"

    printf '%s\n' "$workflow_content" | grep -Fq 'packages: write' ||
        fail "$workflow_name must explicitly own package write permission"

    for forbidden in \
        'docker service update' \
        'docker stack deploy' \
        'scripts/start-production-infra.sh' \
        'scripts/deploy-production-apps.sh' \
        'production_deployment_executor.py --execute' \
        '/opt/mnemos/current' \
        '/etc/mnemos'; do
        if printf '%s\n' "$workflow_content" | grep -Fq "$forbidden"; then
            fail "$workflow_name contains runtime mutation capability: $forbidden"
        fi
    done
}

verify_manual_image_workflow "$WORKFLOW" "Production image batch"
verify_manual_image_workflow "$SERVICE_WORKFLOW" "Production service image workflow"

[ -f "$KEYCLOAK_CLIENTS" ] || fail "Keycloak client configuration is missing"
[ -f "$START_INFRA_SCRIPT" ] || fail "Production infrastructure entrypoint is missing"
[ -f "$DEPLOY_APPS_SCRIPT" ] || fail "Production application deploy entrypoint is missing"

WORKFLOW_CONTENT="$(cat "$WORKFLOW")"
SERVICE_WORKFLOW_CONTENT="$(cat "$SERVICE_WORKFLOW")"

printf '%s\n' "$WORKFLOW_CONTENT" | grep -Fq 'runs-on: [self-hosted, Linux, X64, mnemos-backend]' ||
    fail "Production image publishing must use the hardened backend runner"

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

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'source_sha:' ||
    fail "Production service image workflow must require an explicit immutable source SHA"

if printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'source_ref:'; then
    fail "Production service image workflow must not accept arbitrary Git refs"
fi

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'python3 scripts/production_source_revision.py' ||
    fail "Production service image workflow must validate trusted source history"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq -- '--trusted-ref origin/master' ||
    fail "Production service image workflow must trust only master history"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'git checkout --detach "$SOURCE_SHA"' ||
    fail "Production service image workflow must checkout the immutable source SHA detached"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'if [ "$resolved_sha" != "$SOURCE_SHA" ]; then' ||
    fail "Production service image workflow must verify the detached checkout SHA"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'python3 scripts/publish-production-service-image.py' ||
    fail "Production service image workflow must use the catalog-backed per-service publisher"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq -- '--tag "${{ steps.source.outputs.sha }}"' ||
    fail "Production service image workflow must tag the image with its immutable source SHA"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'DOCKER_CONFIG: ${{ runner.temp }}/mnemos-service-image-docker-auth' ||
    fail "Production service image credentials must use an isolated Docker config"

printf '%s\n' "$SERVICE_WORKFLOW_CONTENT" | grep -Fq 'rm -rf "$DOCKER_CONFIG"' ||
    fail "Production service image credentials must be removed after publishing"

if grep -Fq 'https://app.knalis.rest' "$KEYCLOAK_CLIENTS"; then
    fail "Keycloak client origins must be parameterized instead of hardcoded"
fi

grep -Fq '$(env:MNEMOS_FRONTEND_ORIGIN)' "$KEYCLOAK_CLIENTS" ||
    fail "Keycloak client configuration must use MNEMOS_FRONTEND_ORIGIN"

for deploy_entrypoint in "$START_INFRA_SCRIPT" "$DEPLOY_APPS_SCRIPT"; do
    grep -Fq 'sh "$REPOSITORY_ROOT/scripts/validate-production-env.sh"' "$deploy_entrypoint" ||
        fail "Production runtime entrypoint must validate the complete production environment before mutation: $deploy_entrypoint"
done

echo "Production image workflows, runtime env guards and Keycloak origin boundaries verified"
