#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-backend-service-deploy.yml"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$WORKFLOW" ] || fail "Production backend service deploy workflow is missing"

CONTENT="$(cat "$WORKFLOW")"

printf '%s\n' "$CONTENT" | grep -Fq 'deploy/production-backend/**' ||
    fail "Backend deploy workflow must only trigger from the production backend deploy namespace"

if printf '%s\n' "$CONTENT" | grep -Fq 'workflow_dispatch:'; then
    fail "Backend deploy workflow must not require manual workflow_dispatch authorization"
fi

printf '%s\n' "$CONTENT" | grep -Fq 'group: mnemos-production-deploy' ||
    fail "Backend deploy workflow must share the global production deployment concurrency lock"

printf '%s\n' "$CONTENT" | grep -Fq 'ref: ${{ github.sha }}' ||
    fail "Backend deploy workflow must checkout the exact authorized branch target"

printf '%s\n' "$CONTENT" | grep -Fq 'scripts/production_source_revision.py' ||
    fail "Backend deploy workflow must validate immutable source revisions"

printf '%s\n' "$CONTENT" | grep -Fq -- '--trusted-ref origin/master' ||
    fail "Backend deploy workflow must require target revisions from trusted master history"

printf '%s\n' "$CONTENT" | grep -Fq 'deploy/production-backend/*)' ||
    fail "Backend deploy workflow must derive exactly one service from the authorization ref"

printf '%s\n' "$CONTENT" | grep -Fq 'production_service_catalog.py show "$service"' ||
    fail "Backend deploy workflow must validate the selected service through the production catalog"

printf '%s\n' "$CONTENT" | grep -Fq 'service.get("kind") != "application"' ||
    fail "Backend deploy workflow must reject non-application services"

printf '%s\n' "$CONTENT" | grep -Fq 'production_runtime_state.py' ||
    fail "Backend deploy workflow must verify canonical production runtime state"

printf '%s\n' "$CONTENT" | grep -Fq 'verify-production-availability.sh' ||
    fail "Backend deploy workflow must verify production availability"

printf '%s\n' "$CONTENT" | grep -Fq 'check-production-health.sh' ||
    fail "Backend deploy workflow must verify production health"

printf '%s\n' "$CONTENT" | grep -Fq 'verify-production-telegram-poller.sh' ||
    fail "Backend deploy workflow must verify Telegram singleton state"

printf '%s\n' "$CONTENT" | grep -Fq 'git merge-base --is-ancestor "$current_source_sha" "$TARGET_SHA"' ||
    fail "Backend deploy workflow must refuse automatic service rollback"

printf '%s\n' "$CONTENT" | grep -Fq 'ref: ${{ needs.plan.outputs.tooling_revision }}' ||
    fail "Backend deploy execution must use the tooling revision recorded by production state"

printf '%s\n' "$CONTENT" | grep -Fq 'production_release_plan.py' ||
    fail "Backend deploy workflow must create an audited release plan"

printf '%s\n' "$CONTENT" | grep -Fq 'plan.get("action_count") != 1' ||
    fail "Backend deploy workflow must require exactly one planned runtime action"

printf '%s\n' "$CONTENT" | grep -Fq 'action.get("service") != service' ||
    fail "Backend deploy workflow must bind the plan action to the branch-selected service"

printf '%s\n' "$CONTENT" | grep -Fq 'action.get("operation") != "image-update"' ||
    fail "Backend deploy workflow must only permit image-update actions"

printf '%s\n' "$CONTENT" | grep -Fq './gradlew ":apps:${SERVICE}:check"' ||
    fail "Backend deploy workflow must verify the selected service before image publication"

printf '%s\n' "$CONTENT" | grep -Fq 'publish-production-service-image.py' ||
    fail "Backend deploy workflow must publish the immutable selected service image"

printf '%s\n' "$CONTENT" | grep -Fq 'production_audit_bundle.py' ||
    fail "Backend deploy workflow must create and verify audit bundles"

printf '%s\n' "$CONTENT" | grep -Fq 'production_deployment_executor.py' ||
    fail "Backend deploy workflow must use the scoped production executor"

printf '%s\n' "$CONTENT" | grep -Fq -- '--expected-transition-id "$EXPECTED_TRANSITION_ID"' ||
    fail "Backend deploy workflow must pin execution to the audited transition ID"

printf '%s\n' "$CONTENT" | grep -Fq -- '--execute' ||
    fail "Backend deploy workflow must contain the explicit scoped execution phase"

printf '%s\n' "$CONTENT" | grep -Fq 'MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED' ||
    fail "Backend deploy workflow must preserve the production scoped execution gate"

printf '%s\n' "$CONTENT" | grep -Fq 'environment: production' ||
    fail "Backend deploy mutation must remain behind the GitHub production environment"

printf '%s\n' "$CONTENT" | grep -Fq 'retention-days: 90' ||
    fail "Backend deploy execution audit must use extended retention"

for forbidden in \
    'docker stack deploy' \
    'scripts/start-production-infra.sh' \
    'scripts/deploy-production-apps.sh' \
    '--allow-full-runtime-reconcile' \
    '--requires-full-runtime-reconcile'; do
    if printf '%s\n' "$CONTENT" | grep -Fq -- "$forbidden"; then
        fail "Backend deploy workflow contains forbidden broad runtime capability: $forbidden"
    fi
done

echo "Production backend service deploy security boundary verified"
