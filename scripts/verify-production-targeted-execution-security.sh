#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-targeted-execution.yml"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$WORKFLOW" ] || fail "Targeted production execution workflow is missing"

CONTENT="$(cat "$WORKFLOW")"

printf '%s\n' "$CONTENT" | grep -Fq 'workflow_dispatch:' ||
    fail "Targeted production execution must remain manually dispatched"

if printf '%s\n' "$CONTENT" | grep -Eq '^[[:space:]]+(push|pull_request|schedule|workflow_run):'; then
    fail "Targeted production execution must never gain an automatic trigger"
fi

printf '%s\n' "$CONTENT" | grep -Fq 'group: mnemos-production-deploy' ||
    fail "Targeted production execution must share the global production deployment lock"

printf '%s\n' "$CONTENT" | grep -Fq "vars.MNEMOS_PRODUCTION_EXECUTION_ENABLED == 'true'" ||
    fail "Targeted production execution must be disabled behind the repository execution gate"

printf '%s\n' "$CONTENT" | grep -Fq 'environment: production' ||
    fail "Targeted production execution must require the production GitHub environment"

printf '%s\n' "$CONTENT" | grep -Fq '      - mnemos-production' ||
    fail "Targeted production execution must be isolated to the production runner"

printf '%s\n' "$CONTENT" | grep -Fq 'actions: read' ||
    fail "Targeted production execution must have read-only Actions permission"

printf '%s\n' "$CONTENT" | grep -Fq 'packages: read' ||
    fail "Targeted production execution must only read packages"

printf '%s\n' "$CONTENT" | grep -Fq 'APPLY $EXPECTED_TRANSITION_ID' ||
    fail "Targeted execution must require an exact transition authorization phrase"

printf '%s\n' "$CONTENT" | grep -Fq '"Production Hotfix Plan"|"Production Release Plan"' ||
    fail "Targeted execution must only accept artifacts from approved planning workflows"

printf '%s\n' "$CONTENT" | grep -Fq 'if [ "$event_name" != "workflow_dispatch" ]; then' ||
    fail "Targeted execution must verify planning workflow manual provenance"

printf '%s\n' "$CONTENT" | grep -Fq 'if [ "$head_branch" != "master" ]; then' ||
    fail "Targeted execution must require planning artifacts from master"

printf '%s\n' "$CONTENT" | grep -Fq 'if [ "$conclusion" != "success" ]; then' ||
    fail "Targeted execution must require a successful planning run"

printf '%s\n' "$CONTENT" | grep -Fq 'ref: ${{ inputs.tooling_revision }}' ||
    fail "Targeted execution must checkout the explicitly pinned tooling revision"

printf '%s\n' "$CONTENT" | grep -Fq 'actions/download-artifact@d3f86a106a0bac45b974a628896c90dbdf5c8093' ||
    fail "Targeted execution must use the pinned cross-run artifact downloader"

printf '%s\n' "$CONTENT" | grep -Fq 'run-id: ${{ inputs.plan_run_id }}' ||
    fail "Targeted execution must pin the exact planning workflow run"

printf '%s\n' "$CONTENT" | grep -Fq 'production_audit_bundle.py' ||
    fail "Targeted execution must verify artifact integrity before execution"

printf '%s\n' "$CONTENT" | grep -Fq 'plan.get("mode") != "scoped"' ||
    fail "Targeted execution must reject full runtime reconcile plans"

printf '%s\n' "$CONTENT" | grep -Fq 'MNEMOS_PRODUCTION_DEPLOY_ENABLED: ${{ vars.MNEMOS_PRODUCTION_DEPLOY_ENABLED }}' ||
    fail "Targeted execution must retain the base executor gate"

printf '%s\n' "$CONTENT" | grep -Fq 'MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED: ${{ vars.MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED }}' ||
    fail "Targeted execution must retain the scoped executor gate"

printf '%s\n' "$CONTENT" | grep -Fq -- '--expected-transition-id "$EXPECTED_TRANSITION_ID"' ||
    fail "Targeted execution must pin the exact transition ID at executor invocation"

printf '%s\n' "$CONTENT" | grep -Fq -- '--execute' ||
    fail "Targeted execution workflow must explicitly invoke executor execution mode"

printf '%s\n' "$CONTENT" | grep -Fq 'retention-days: 90' ||
    fail "Targeted execution audit records must have extended retention"

printf '%s\n' "$CONTENT" | grep -Fq 'if: always()' ||
    fail "Targeted execution must clean credentials even after failure"

for forbidden in \
    'docker stack deploy' \
    'scripts/start-production-infra.sh' \
    'scripts/deploy-production-apps.sh' \
    'scripts/check-production-health.sh'; do
    if printf '%s\n' "$CONTENT" | grep -Fq "$forbidden"; then
        fail "Targeted production execution contains full-stack mutation path: $forbidden"
    fi
done

DRY_RUN_LINE="$(grep -n -F 'Verify current production state and render exact dry run' "$WORKFLOW" | head -n 1 | cut -d: -f1)"
EXECUTE_LINE="$(grep -n -F 'Execute exact scoped transition' "$WORKFLOW" | head -n 1 | cut -d: -f1)"

if [ -z "$DRY_RUN_LINE" ] || [ -z "$EXECUTE_LINE" ] || [ "$DRY_RUN_LINE" -ge "$EXECUTE_LINE" ]; then
    fail "Targeted execution dry-run validation must precede runtime execution"
fi

echo "Targeted production execution workflow security boundary verified"
