#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
EXECUTOR="$REPOSITORY_ROOT/scripts/production_deployment_executor.py"
BASE_EXECUTOR="$REPOSITORY_ROOT/scripts/production_deployment_executor_base.py"
BASELINE_VERIFIER="$REPOSITORY_ROOT/scripts/verify-production-runtime-baseline.py"
AVAILABILITY_VERIFIER="$REPOSITORY_ROOT/scripts/verify-production-availability.sh"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$EXECUTOR" ] || fail "Scoped production deployment executor is missing"
[ -f "$BASE_EXECUTOR" ] || fail "Scoped production deployment executor base is missing"
[ -f "$BASELINE_VERIFIER" ] || fail "Production runtime baseline verifier is missing"
[ -f "$AVAILABILITY_VERIFIER" ] || fail "Production availability verifier is missing"

EXECUTOR_CONTENT="$(cat "$EXECUTOR"; cat "$BASE_EXECUTOR")"
WRAPPER_CONTENT="$(cat "$EXECUTOR")"

for required in \
    'MNEMOS_PRODUCTION_DEPLOY_ENABLED' \
    'MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED' \
    'require_root()' \
    '--expected-transition-id' \
    'arguments.expected_transition_id != transition["transition_id"]' \
    'fcntl.LOCK_EX | fcntl.LOCK_NB' \
    'Scoped executor refuses full runtime reconcile plans' \
    'Scoped service deployment cannot change the production tooling revision' \
    'verify-production-runtime-baseline.py' \
    'status="preflight-failed"' \
    'for action in reversed(attempted)' \
    'rollback_steps(action)' \
    'create_json_exclusive(path, record)' \
    'state_dir / "transitions" / transition_id / attempt_name' \
    'atomic_write_json(state_dir / "current-state.json", target_state)'; do
    if ! printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq -- "$required"; then
        fail "Scoped production executor is missing required safety property: $required"
    fi
done

for wrapper_required in \
    'verify-production-availability.sh' \
    'run_steps(availability_steps())' \
    'base.rollback_steps(action) + availability_steps()'; do
    if ! printf '%s\n' "$WRAPPER_CONTENT" | grep -Fq -- "$wrapper_required"; then
        fail "Availability-safe executor wrapper is missing required property: $wrapper_required"
    fi
done

lock_line="$(grep -n -F 'fcntl.flock(' "$BASE_EXECUTOR" | tail -n 1 | cut -d: -f1)"
execution_context_line="$(
    grep -n -F 'current_state, target_state, transition = read_execution_context(' "$BASE_EXECUTOR" \
        | tail -n 1 \
        | cut -d: -f1
)"

if [ -z "$lock_line" ] || [ -z "$execution_context_line" ]; then
    fail "Scoped executor lock/state ordering could not be verified"
fi

if [ "$lock_line" -ge "$execution_context_line" ]; then
    fail "Execute-mode must acquire the deployment lock before reading current state"
fi

if printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq '"stack", "deploy"'; then
    fail "Scoped production executor must never use docker stack deploy"
fi

if printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq 'wait-production-stack.sh'; then
    fail "Scoped production executor must not wait for the entire production stack"
fi

if ! printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq '"--no-deps"'; then
    fail "Scoped Compose deployment must retain the --no-deps boundary"
fi

if ! printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq 'wait-production-service.sh'; then
    fail "Scoped Swarm deployment must use service-specific convergence checks"
fi

if ! printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq 'wait-production-container.sh'; then
    fail "Scoped Compose deployment must use container-specific health checks"
fi

echo "Scoped production deployment executor safety properties verified"
