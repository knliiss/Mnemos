#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
REQUEST_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-backend-deploy-request.yml"
DEPLOY_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-backend-service-deploy.yml"
DEPLOY_EXECUTOR="$REPOSITORY_ROOT/scripts/production_deployment_executor.py"
RUNTIME_STATE="$REPOSITORY_ROOT/scripts/production_runtime_state.py"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$REQUEST_WORKFLOW" ] || fail "Production backend deploy request workflow is missing"
[ -f "$DEPLOY_WORKFLOW" ] || fail "Production backend service deploy workflow is missing"
[ -f "$DEPLOY_EXECUTOR" ] || fail "Production deployment executor is missing"
[ -f "$RUNTIME_STATE" ] || fail "Production runtime state observer is missing"

REQUEST_CONTENT="$(cat "$REQUEST_WORKFLOW")"
DEPLOY_CONTENT="$(cat "$DEPLOY_WORKFLOW")"

printf '%s\n' "$REQUEST_CONTENT" | grep -Fq 'deploy/production-backend/**' ||
    fail "Backend deploy request must only trigger from the production backend deploy namespace"

printf '%s\n' "$REQUEST_CONTENT" | grep -Fq 'runs-on: [self-hosted, Linux, X64, mnemos-backend]' ||
    fail "Backend deploy request must stay on the hardened non-production backend runner"

if printf '%s\n' "$REQUEST_CONTENT" | grep -Fq 'workflow_dispatch:'; then
    fail "Backend deploy request must not require manual workflow_dispatch authorization"
fi

for forbidden in \
    'mnemos-production' \
    'packages: write' \
    'environment: production' \
    'docker login' \
    'docker service update' \
    'docker stack deploy' \
    'production_deployment_executor.py' \
    'production_release_plan.py' \
    'publish-production-service-image.py'; do
    if printf '%s\n' "$REQUEST_CONTENT" | grep -Fq "$forbidden"; then
        fail "Read-only backend deploy request contains privileged capability: $forbidden"
    fi
done

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'workflow_run:' ||
    fail "Backend deployment must be downstream of a workflow_run event from trusted master"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'Production Backend Deploy Request' ||
    fail "Backend deployment must watch the read-only deploy request workflow"

if printf '%s\n' "$DEPLOY_CONTENT" | grep -Eq '^[[:space:]]+(push|pull_request|schedule|workflow_dispatch):'; then
    fail "Privileged backend deployment must not have a direct trigger"
fi

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'group: mnemos-production-deploy' ||
    fail "Backend deploy workflow must share the global production deployment concurrency lock"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'actions: read' ||
    fail "Backend deploy workflow must have read access for request provenance verification"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'ref: ${{ github.sha }}' ||
    fail "Backend deploy workflow must checkout the exact trusted coordinator revision"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'git merge-base --is-ancestor "$COORDINATOR_SHA" origin/master' ||
    fail "Backend deploy coordinator must remain in trusted master history"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'urllib.request.Request' ||
    fail "Backend deploy workflow must use Python stdlib for request provenance retrieval"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq '/actions/runs/{request_run_id}' ||
    fail "Backend deploy workflow must independently fetch request run provenance"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq '.github/workflows/production-backend-deploy-request.yml' ||
    fail "Backend deploy workflow must require the exact authorized request workflow path"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'if event_name != "push":' ||
    fail "Backend deploy workflow must require a push-origin request"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'if head_repository != repository:' ||
    fail "Backend deploy workflow must reject requests from another repository"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'deploy/production-backend/*)' ||
    fail "Backend deploy workflow must derive exactly one service from the authorization ref"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'scripts/production_source_revision.py' ||
    fail "Backend deploy workflow must validate immutable source revisions"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq -- '--trusted-ref origin/master' ||
    fail "Backend deploy workflow must require target revisions from trusted master history"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'production_service_catalog.py show "$service"' ||
    fail "Backend deploy workflow must validate the selected service through the production catalog"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'service.get("kind") != "application"' ||
    fail "Backend deploy workflow must reject non-application services"

if printf '%s\n' "$DEPLOY_CONTENT" | grep -Eq '(^|[^[:alnum:]_])jq([^[:alnum:]_]|$)'; then
    fail "Backend deploy workflow must not depend on jq being installed on a runner"
fi

if printf '%s\n' "$DEPLOY_CONTENT" | grep -Eq '(^|[[:space:]])curl([[:space:]]|$)'; then
    fail "Backend deploy provenance path must not depend on curl being installed"
fi

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'production_runtime_state.py' ||
    fail "Backend deploy workflow must verify canonical production runtime state"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'verify-production-availability.sh' ||
    fail "Backend deploy workflow must verify production availability"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'check-production-health.sh' ||
    fail "Backend deploy workflow must verify production health"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'verify-production-telegram-poller.sh' ||
    fail "Backend deploy workflow must verify Telegram singleton state"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'git merge-base --is-ancestor "$current_source_sha" "$TARGET_SHA"' ||
    fail "Backend deploy workflow must refuse automatic service rollback"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'production_release_plan.py' ||
    fail "Backend deploy workflow must create an audited release plan"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'plan.get("action_count") != 1' ||
    fail "Backend deploy workflow must require exactly one planned runtime action"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'action.get("service") != service' ||
    fail "Backend deploy workflow must bind the plan action to the branch-selected service"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'action.get("operation") != "image-update"' ||
    fail "Backend deploy workflow must only permit image-update actions"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'name: Verify target CI gates' ||
    fail "Backend deploy workflow must verify trusted CI gates for the exact target SHA"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq '"Backend Verify": ".github/workflows/backend-verify.yml"' ||
    fail "Backend deploy workflow must require the Backend Verify push workflow for the target SHA"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'scripts/resolve_backend_ci_changes.py --paths-file "$changed_paths"' ||
    fail "Backend deploy workflow must classify the exact target diff before deciding Stack Smoke requirements"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'if os.environ["STACK_SMOKE_REQUIRED"] == "true":' ||
    fail "Backend deploy workflow must require Stack Smoke only when target policy marks it required"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'expected["Stack Smoke"] = ".github/workflows/stack-smoke.yml"' ||
    fail "Backend deploy workflow must preserve the conditional trusted Stack Smoke gate"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'if os.environ["INTEGRATION_VERIFY_REQUIRED"] == "true":' ||
    fail "Backend deploy workflow must require Integration Verify only for integration-sensitive target diffs"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'expected["Integration Verify"] = ".github/workflows/integration-verify.yml"' ||
    fail "Backend deploy workflow must preserve the conditional trusted Integration Verify gate"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'run.get("head_branch") == "master"' ||
    fail "Backend deploy CI gates must come from trusted master push runs"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'latest.get("conclusion") != "success"' ||
    fail "Backend deploy workflow must reject unsuccessful target CI runs"

if printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq './gradlew ":apps:${SERVICE}:check"'; then
    fail "Backend deploy workflow must not rerun service checks already proven by target CI"
fi

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'publish-production-service-image.py' ||
    fail "Backend deploy workflow must publish the immutable selected service image"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'ref: ${{ needs.plan.outputs.tooling_revision }}' ||
    fail "Backend deploy execution must use the tooling revision recorded by production state"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'production_audit_bundle.py' ||
    fail "Backend deploy workflow must create and verify audit bundles"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'production_deployment_executor.py' ||
    fail "Backend deploy workflow must use the scoped production executor"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq -- '--expected-transition-id "$EXPECTED_TRANSITION_ID"' ||
    fail "Backend deploy workflow must pin execution to the audited transition ID"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq -- '--execute' ||
    fail "Backend deploy workflow must contain the explicit scoped execution phase"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED' ||
    fail "Backend deploy workflow must preserve the production scoped execution gate"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'environment: production' ||
    fail "Backend deploy mutation must remain behind the GitHub production environment"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq '"authorization_mechanism": "service-deploy-branch-workflow-run"' ||
    fail "Backend deploy audit must record the trusted workflow_run authorization chain"

# The scoped backend deploy uses the hardened production runner's durable
# root-only spool; Actions artifact storage quotas must not halt production.
for required in \
    'name: Securely persist approved plan on production runner' \
    'name: Restore immutable approved plan from production runner' \
    'name: Archive execution audit on production runner' \
    'approval_runner: ${{ steps.plan.outputs.approval_runner }}' \
    '[ "$RUNNER_NAME" != "$APPROVAL_RUNNER" ]' \
    '/var/lib/mnemos-deploy/approved-backend-plans' \
    '/var/lib/mnemos-deploy/backend-execution-audits' \
    'sudo -n chown -R root:root "$spool"' \
    'sudo -n chown -R root:root "$audit_archive"' \
    'sudo -n chmod -R go-rwx "$spool"' \
    'sudo -n chmod -R go-rwx "$audit_archive"' \
    '--kind release-plan' \
    '--kind execution-attempt' \
    'Approved plan is bound to a different production runner'; do
    printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq -- "$required" ||
        fail "Production backend deploy lacks root-only verified local handoff: $required"
done

if printf '%s\n' "$DEPLOY_CONTENT" | grep -Eq 'actions/(upload|download)-artifact@'; then
    fail "Scoped backend deploy must not require quota-limited Actions artifacts"
fi

workspace_hygiene_count="$(printf '%s\n' "$DEPLOY_CONTENT" | grep -Fc 'name: Prepare persistent production runner workspace' || true)"
[ "$workspace_hygiene_count" -eq 2 ] ||
    fail "Persistent production runner workspace hygiene must run before both production checkouts"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq 'sudo -n chown -R "$(id -u):$(id -g)" "$GITHUB_WORKSPACE"' ||
    fail "Persistent production runner workspace ownership repair is missing"

printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq -- '-name __pycache__' ||
    fail "Persistent production runner bytecode cache cleanup is missing"

first_hygiene_line="$(printf '%s\n' "$DEPLOY_CONTENT" | grep -n -m1 -F 'name: Prepare persistent production runner workspace' | cut -d: -f1)"
coordinator_checkout_line="$(printf '%s\n' "$DEPLOY_CONTENT" | grep -n -m1 -F 'name: Checkout trusted deployment coordinator' | cut -d: -f1)"
last_hygiene_line="$(printf '%s\n' "$DEPLOY_CONTENT" | grep -n -F 'name: Prepare persistent production runner workspace' | tail -n 1 | cut -d: -f1)"
pinned_checkout_line="$(printf '%s\n' "$DEPLOY_CONTENT" | grep -n -m1 -F 'name: Checkout pinned production deployment tooling' | cut -d: -f1)"

[ "$first_hygiene_line" -lt "$coordinator_checkout_line" ] ||
    fail "Plan runner hygiene must execute before trusted coordinator checkout"
[ "$last_hygiene_line" -lt "$pinned_checkout_line" ] ||
    fail "Execution runner hygiene must execute before pinned tooling checkout"

privileged_bytecode_env_count="$(printf '%s\n' "$DEPLOY_CONTENT" | grep -Fc 'PYTHONDONTWRITEBYTECODE=1' || true)"
[ "$privileged_bytecode_env_count" -ge 5 ] ||
    fail "Privileged production Python calls must disable bytecode writes for pinned tooling compatibility"

executor_bytecode_line="$(grep -n -m1 -F 'sys.dont_write_bytecode = True' "$DEPLOY_EXECUTOR" | cut -d: -f1 || true)"
executor_local_import_line="$(grep -n -m1 -F 'import production_deployment_executor_base as base' "$DEPLOY_EXECUTOR" | cut -d: -f1 || true)"

[ -n "$executor_bytecode_line" ] ||
    fail "Privileged deployment executor must disable Python bytecode caches"
[ -n "$executor_local_import_line" ] ||
    fail "Privileged deployment executor local import boundary is missing"
[ "$executor_bytecode_line" -lt "$executor_local_import_line" ] ||
    fail "Deployment executor must disable bytecode caches before local imports"

runtime_bytecode_line="$(grep -n -m1 -F 'sys.dont_write_bytecode = True' "$RUNTIME_STATE" | cut -d: -f1 || true)"
runtime_local_import_line="$(grep -n -m1 -F 'from production_deployment_state import' "$RUNTIME_STATE" | cut -d: -f1 || true)"

[ -n "$runtime_bytecode_line" ] ||
    fail "Privileged runtime state observer must disable Python bytecode caches"
[ -n "$runtime_local_import_line" ] ||
    fail "Privileged runtime state observer local import boundary is missing"
[ "$runtime_bytecode_line" -lt "$runtime_local_import_line" ] ||
    fail "Runtime state observer must disable bytecode caches before local imports"

for forbidden in \
    'docker stack deploy' \
    'scripts/start-production-infra.sh' \
    'scripts/deploy-production-apps.sh' \
    '--allow-full-runtime-reconcile' \
    '--requires-full-runtime-reconcile'; do
    if printf '%s\n' "$DEPLOY_CONTENT" | grep -Fq -- "$forbidden"; then
        fail "Backend deploy workflow contains forbidden broad runtime capability: $forbidden"
    fi
done

echo "Production backend service deploy security boundary verified"
