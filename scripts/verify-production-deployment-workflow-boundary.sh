#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW_DIR="$REPOSITORY_ROOT/.github/workflows"

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

for removed_workflow in \
    cancel-stale-production-recovery.yml \
    production-account-login-hotfix.yml \
    production-recovery.yml; do
    if [ -e "$WORKFLOW_DIR/$removed_workflow" ]; then
        fail "Incident-only production workflow must not be restored: $removed_workflow"
    fi
done

for workflow in \
    production-hotfix-plan.yml \
    production-images.yml \
    production-service-image.yml; do
    path="$WORKFLOW_DIR/$workflow"
    [ -f "$path" ] || fail "Expected production workflow is missing: $workflow"

    content="$(cat "$path")"

    if ! printf '%s\n' "$content" | grep -Fq 'workflow_dispatch:'; then
        fail "Production workflow must remain manually dispatched: $workflow"
    fi

    if printf '%s\n' "$content" | grep -Eq '^[[:space:]]+(push|pull_request|schedule|workflow_run):'; then
        fail "Production workflow must not gain an automatic trigger: $workflow"
    fi

    if uses_production_runner "$path"; then
        fail "CI-safe production workflow must not target the production runner: $workflow"
    fi
done

HOTFIX_PLAN_PATH="$WORKFLOW_DIR/production-hotfix-plan.yml"
HOTFIX_PLAN_CONTENT="$(cat "$HOTFIX_PLAN_PATH")"

for forbidden in \
    'docker service update' \
    'docker stack deploy' \
    'production-infra.sh up' \
    'production_deployment_executor.py --execute'; do
    if printf '%s\n' "$HOTFIX_PLAN_CONTENT" | grep -Fq "$forbidden"; then
        fail "Plan-only hotfix workflow contains production mutation command: $forbidden"
    fi
done

IMAGE_BATCH_PATH="$WORKFLOW_DIR/production-images.yml"
IMAGE_BATCH_CONTENT="$(cat "$IMAGE_BATCH_PATH")"

for forbidden in \
    'docker service update' \
    'docker stack deploy' \
    'scripts/start-production-infra.sh' \
    'scripts/deploy-production-apps.sh' \
    'production_deployment_executor.py --execute'; do
    if printf '%s\n' "$IMAGE_BATCH_CONTENT" | grep -Fq "$forbidden"; then
        fail "Image-only workflow contains production mutation command: $forbidden"
    fi
done

echo "Production deployment workflow safety boundary verified"
