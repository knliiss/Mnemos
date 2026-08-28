#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
TEMP_DIR="$(mktemp -d)"

cleanup() {
    rm -rf "$TEMP_DIR"
}

trap cleanup EXIT INT TERM

TEST_ENV="$TEMP_DIR/production.env"
STACK_JSON="$TEMP_DIR/stack.json"

cp "$REPOSITORY_ROOT/.env.example" "$TEST_ENV"

export MNEMOS_PRODUCTION_ENV_FILE="$TEST_ENV"
export MNEMOS_IMAGE_REGISTRY="ghcr.io/knliiss"
export MNEMOS_RELEASE_TAG="0123456789abcdef"
export MNEMOS_SWARM_NETWORK="mnemos-production-network"
export MNEMOS_KAFKA_TRUSTSTORE_CONFIG="mnemos-kafka-truststore-deadbeef"
export MNEMOS_CLOUDFLARE_TUNNEL_SECRET="mnemos-cloudflare-tunnel-token-deadbeef"

sh "$SCRIPT_DIR/render-production-app-stack.sh" "$STACK_JSON"

python3 - "$STACK_JSON" <<'PY'
import json
import sys
from pathlib import Path


stack = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
services = stack["services"]

expected_pool_sizes = {
    "account-service": 4,
    "wallet-service": 4,
    "event-service": 4,
    "notification-service": 3,
    "reward-service": 3,
    "bet-service": 3,
    "moderation-service": 2,
    "realtime-service": 3,
    "personalization-service": 3,
    "social-service": 3,
    "profile-service": 3,
    "player-directory-service": 4,
    "payment-service": 3,
}

expected_defaults = {
    "SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE": "1",
    "SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT": "5s",
    "SPRING_DATASOURCE_HIKARI_VALIDATION_TIMEOUT": "3s",
    "SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT": "60s",
    "SPRING_DATASOURCE_HIKARI_MAX_LIFETIME": "30m",
    "SPRING_DATASOURCE_HIKARI_KEEPALIVE_TIME": "5m",
}


def fail(message: str) -> None:
    raise SystemExit(message)


connection_budget = 0

for service_name, expected_pool_size in expected_pool_sizes.items():
    service = services[service_name]
    environment = service.get("environment", {})

    for key, expected_value in expected_defaults.items():
        if environment.get(key) != expected_value:
            fail(f"{service_name} has unexpected {key}: {environment.get(key)!r}")

    actual_pool_size = environment.get("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE")

    if actual_pool_size != str(expected_pool_size):
        fail(
            f"{service_name} has unexpected maximum pool size: "
            f"{actual_pool_size!r}"
        )

    deploy = service.get("deploy", {})
    replicas = deploy.get("replicas")

    if not isinstance(replicas, int) or replicas < 1:
        fail(f"{service_name} has invalid replica count: {replicas!r}")

    connection_budget += replicas * expected_pool_size

if connection_budget > 90:
    fail(
        "Application PostgreSQL connection budget exceeds the production ceiling: "
        f"{connection_budget} > 90"
    )

if "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE" in services["gateway-service"].get("environment", {}):
    fail("gateway-service must not receive datasource pool configuration")

print(
    "Production application datasource pools verified; "
    f"maximum configured connection budget={connection_budget}"
)
PY
