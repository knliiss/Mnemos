#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
EXECUTOR="$REPOSITORY_ROOT/scripts/production_deployment_executor.py"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$EXECUTOR" ] || fail "Scoped production deployment executor is missing"

EXECUTOR_CONTENT="$(cat "$EXECUTOR")"

for required in \
    'MNEMOS_PRODUCTION_DEPLOY_ENABLED' \
    'MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED' \
    'require_root()' \
    '--expected-transition-id' \
    'arguments.expected_transition_id != transition["transition_id"]' \
    'fcntl.LOCK_EX | fcntl.LOCK_NB' \
    'Scoped executor refuses full runtime reconcile plans' \
    'Scoped service deployment cannot change the production tooling revision' \
    'for action in reversed(attempted)' \
    'rollback_steps(action)' \
    'create_json_exclusive(path, record)' \
    'atomic_write_json(state_dir / "current-state.json", target_state)'; do
    if ! printf '%s\n' "$EXECUTOR_CONTENT" | grep -Fq -- "$required"; then
        fail "Scoped production executor is missing required safety property: $required"
    fi
done

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
