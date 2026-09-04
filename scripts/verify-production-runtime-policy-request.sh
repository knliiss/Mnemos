#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
REQUEST_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-runtime-policy-request.yml"
RUNTIME_WORKFLOW="$REPOSITORY_ROOT/.github/workflows/production-runtime-policy.yml"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$REQUEST_WORKFLOW" ] || fail "Runtime policy request workflow is missing"
[ -f "$RUNTIME_WORKFLOW" ] || fail "Runtime policy workflow is missing"

REQUEST_CONTENT="$(cat "$REQUEST_WORKFLOW")"
RUNTIME_CONTENT="$(cat "$RUNTIME_WORKFLOW")"

printf '%s\n' "$REQUEST_CONTENT" | grep -Fq 'deploy/production-runtime-policy' ||
    fail "Runtime policy request must use the dedicated deployment ref"

printf '%s\n' "$REQUEST_CONTENT" | grep -Fq 'runs-on: ubuntu-24.04' ||
    fail "Runtime policy request must run on a hosted runner"

if printf '%s\n' "$REQUEST_CONTENT" | grep -Fq 'mnemos-production'; then
    fail "Runtime policy request must not have production runner access"
fi

for forbidden in \
    'scripts/production-infra.sh' \
    'scripts/reconcile-production-runtime-policy.py' \
    'scripts/install-production-runtime.sh' \
    'docker service update' \
    'docker stack deploy' \
    '/etc/mnemos'; do
    if printf '%s\n' "$REQUEST_CONTENT" | grep -Fq "$forbidden"; then
        fail "Runtime policy request contains production mutation capability: $forbidden"
    fi
done

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq 'workflow_run:' ||
    fail "Runtime policy mutation workflow must use workflow_run authorization"

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq 'Production Runtime Policy Request' ||
    fail "Runtime policy mutation workflow must trust only the request workflow"

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq '.github/workflows/production-runtime-policy-request.yml' ||
    fail "Runtime policy mutation workflow must verify the exact request workflow path"

if printf '%s\n' "$RUNTIME_CONTENT" | grep -Eq '^[[:space:]]{2}push:[[:space:]]*$'; then
    fail "Runtime policy mutation workflow must not run directly from a push"
fi

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq 'scripts/production-infra.sh' ||
    fail "Runtime policy workflow must reconcile declarative infrastructure configuration"

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq 'keycloak-config' ||
    fail "Runtime policy workflow must apply declarative Keycloak configuration"

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq 'mnemos_user_id:__mnemos_permission_probe__' ||
    fail "Runtime policy workflow must verify the moderation user-query permission"

printf '%s\n' "$RUNTIME_CONTENT" | grep -Fq 'verify-production-telegram-poller.sh' ||
    fail "Runtime policy workflow must preserve Telegram singleton verification"

echo "Production runtime policy request boundary verified"
