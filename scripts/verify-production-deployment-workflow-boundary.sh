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
    production-release-plan.yml \
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

verify_no_runtime_mutation() {
    workflow_path="$1"
    description="$2"
    content="$(cat "$workflow_path")"

    for forbidden in \
        'docker service update' \
        'docker stack deploy' \
        'scripts/start-production-infra.sh' \
        'scripts/deploy-production-apps.sh' \
        'production-infra.sh up' \
        'production_deployment_executor.py --execute' \
        '/opt/mnemos/current' \
        '/etc/mnemos'; do
        if printf '%s\n' "$content" | grep -Fq "$forbidden"; then
            fail "$description contains production mutation capability: $forbidden"
        fi
    done
}

verify_trusted_revision_validation() {
    workflow_path="$1"
    description="$2"
    content="$(cat "$workflow_path")"

    printf '%s\n' "$content" | grep -Fq 'scripts/production_source_revision.py' ||
        fail "$description must validate target revisions with production_source_revision.py"

    printf '%s\n' "$content" | grep -Fq -- '--trusted-ref origin/master' ||
        fail "$description must validate target revisions against trusted origin/master history"
}

verify_exact_planning_revision() {
    workflow_path="$1"
    description="$2"
    content="$(cat "$workflow_path")"

    printf '%s\n' "$content" | grep -Fq 'ref: ${{ github.sha }}' ||
        fail "$description must checkout the exact workflow run head SHA"

    printf '%s\n' "$content" | grep -Fq 'PLANNING_HEAD_SHA: ${{ github.sha }}' ||
        fail "$description must explicitly verify the workflow run head SHA"

    printf '%s\n' "$content" | grep -Fq '$(git rev-parse HEAD)' ||
        fail "$description must compare checked-out tooling to the workflow run head SHA"

    printf '%s\n' "$content" | grep -Fq 'git merge-base --is-ancestor "$PLANNING_HEAD_SHA" origin/master' ||
        fail "$description must require planning tooling to remain in trusted master history"

    if printf '%s\n' "$content" | grep -Eq '^[[:space:]]+ref:[[:space:]]+master[[:space:]]*$'; then
        fail "$description must not dynamically checkout the moving master ref"
    fi
}

verify_no_runtime_mutation \
    "$WORKFLOW_DIR/production-hotfix-plan.yml" \
    "Plan-only hotfix workflow"

verify_no_runtime_mutation \
    "$WORKFLOW_DIR/production-images.yml" \
    "Image-only workflow"

verify_no_runtime_mutation \
    "$WORKFLOW_DIR/production-release-plan.yml" \
    "Release plan workflow"

verify_trusted_revision_validation \
    "$WORKFLOW_DIR/production-hotfix-plan.yml" \
    "Plan-only hotfix workflow"

verify_trusted_revision_validation \
    "$WORKFLOW_DIR/production-release-plan.yml" \
    "Release plan workflow"

verify_exact_planning_revision \
    "$WORKFLOW_DIR/production-hotfix-plan.yml" \
    "Plan-only hotfix workflow"

verify_exact_planning_revision \
    "$WORKFLOW_DIR/production-release-plan.yml" \
    "Release plan workflow"

RELEASE_PLAN_CONTENT="$(cat "$WORKFLOW_DIR/production-release-plan.yml")"

printf '%s\n' "$RELEASE_PLAN_CONTENT" | grep -Fq 'requires_full_runtime_reconcile:' ||
    fail "Release plan workflow must record whether a full runtime reconcile is required"

printf '%s\n' "$RELEASE_PLAN_CONTENT" | grep -Fq 'allow_full_runtime_reconcile:' ||
    fail "Release plan workflow must require a separate full runtime reconcile allow gate"

printf '%s\n' "$RELEASE_PLAN_CONTENT" | grep -Fq 'retention-days: 30' ||
    fail "Release plan audit artifact must use extended retention"

echo "Production deployment workflow safety boundary verified"
