#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

REPOSITORY_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
ENV_FILE="${MNEMOS_PRODUCTION_ENV_FILE:-/etc/mnemos/mnemos.env}"
STACK_NAME="${MNEMOS_SWARM_STACK_NAME:-mnemos}"
SWARM_NETWORK="${MNEMOS_SWARM_NETWORK:-mnemos-production-network}"
IMAGE_REGISTRY="${MNEMOS_IMAGE_REGISTRY:?MNEMOS_IMAGE_REGISTRY is required}"
RELEASE_TAG="${MNEMOS_RELEASE_TAG:?MNEMOS_RELEASE_TAG is required}"
DEPLOY_STATE_DIR="${MNEMOS_DEPLOY_STATE_DIR:-/var/lib/mnemos-deploy}"

if [ ! -f "$ENV_FILE" ]; then
    echo "Production environment file is missing: $ENV_FILE" >&2
    exit 1
fi

swarm_state="$(docker info --format '{{ .Swarm.LocalNodeState }}')"
manager_control="$(docker info --format '{{ .Swarm.ControlAvailable }}')"

if [ "$swarm_state" != "active" ] || [ "$manager_control" != "true" ]; then
    echo "This node is not an active Docker Swarm manager" >&2
    exit 1
fi

network_driver="$(docker network inspect --format '{{ .Driver }}' "$SWARM_NETWORK" 2>/dev/null || true)"
network_scope="$(docker network inspect --format '{{ .Scope }}' "$SWARM_NETWORK" 2>/dev/null || true)"
network_attachable="$(docker network inspect --format '{{ .Attachable }}' "$SWARM_NETWORK" 2>/dev/null || true)"

if [ "$network_driver" != "overlay" ] || [ "$network_scope" != "swarm" ] || [ "$network_attachable" != "true" ]; then
    echo "Production network must be an attachable Swarm overlay: $SWARM_NETWORK" >&2
    exit 1
fi

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-mnemos}"
export COMPOSE_PROJECT_NAME

sh "$REPOSITORY_ROOT/scripts/verify-production-infra.sh"

KAFKA_TRUSTSTORE_CONFIG="$(
    sh "$REPOSITORY_ROOT/scripts/sync-production-kafka-truststore.sh"
)"

CLOUDFLARE_TUNNEL_SECRET="$(
    sh "$REPOSITORY_ROOT/scripts/sync-production-cloudflare-tunnel-secret.sh"
)"

export MNEMOS_IMAGE_REGISTRY="$IMAGE_REGISTRY"
export MNEMOS_RELEASE_TAG="$RELEASE_TAG"
export MNEMOS_SWARM_NETWORK="$SWARM_NETWORK"
export MNEMOS_KAFKA_TRUSTSTORE_CONFIG="$KAFKA_TRUSTSTORE_CONFIG"
export MNEMOS_CLOUDFLARE_TUNNEL_SECRET="$CLOUDFLARE_TUNNEL_SECRET"
export MNEMOS_PRODUCTION_ENV_FILE="$ENV_FILE"

umask 077
STACK_FILE="$(mktemp)"

cleanup() {
    rm -f "$STACK_FILE"
}

trap cleanup EXIT INT TERM

sh "$REPOSITORY_ROOT/scripts/render-production-app-stack.sh" "$STACK_FILE"

docker stack config --compose-file "$STACK_FILE" >/dev/null

echo "Deploying Mnemos release ${RELEASE_TAG}"

docker stack deploy \
    --compose-file "$STACK_FILE" \
    --with-registry-auth \
    --resolve-image always \
    --prune \
    "$STACK_NAME"

if ! sh "$REPOSITORY_ROOT/scripts/wait-production-stack.sh" \
    "$STACK_NAME" \
    "$RELEASE_TAG" \
    "${MNEMOS_DEPLOY_TIMEOUT_SECONDS:-900}"; then
    echo "Release ${RELEASE_TAG} did not converge. Swarm update policies will retain or roll back services according to their deployment state." >&2
    exit 1
fi

sh "$REPOSITORY_ROOT/scripts/prune-production-cloudflare-tunnel-secrets.sh" \
    "$CLOUDFLARE_TUNNEL_SECRET"

mkdir -p "$DEPLOY_STATE_DIR"
printf '%s\n' "$RELEASE_TAG" > "$DEPLOY_STATE_DIR/current-release"
chmod 600 "$DEPLOY_STATE_DIR/current-release"

echo "Mnemos production release ${RELEASE_TAG} deployed successfully"
