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
CONFIG_JSON="$TEMP_DIR/config.json"

cp "$REPOSITORY_ROOT/.env.example" "$TEST_ENV"

export MNEMOS_PRODUCTION_ENV_FILE="$TEST_ENV"
export MNEMOS_IMAGE_REGISTRY="ghcr.io/knliiss"
export MNEMOS_RELEASE_TAG="0123456789abcdef"
export MNEMOS_SWARM_NETWORK="mnemos-production-network"

sh "$SCRIPT_DIR/production-infra.sh" \
    config \
    --format json \
    > "$CONFIG_JSON"

python3 - "$CONFIG_JSON" <<'PY'
import json
import sys
from pathlib import Path


config = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
services = config["services"]


def fail(message: str) -> None:
    raise SystemExit(message)


def require_no_field(service_name: str, field: str) -> None:
    if field in services[service_name]:
        fail(f"{service_name} unexpectedly contains {field}")


def require_command_setting(service_name: str, setting: str) -> None:
    command = services[service_name].get("command", [])

    if setting not in command:
        fail(f"{service_name} is missing PostgreSQL setting {setting}")


def require_memory_limit(service_name: str, minimum_bytes: int, maximum_bytes: int) -> None:
    raw_memory = services[service_name].get("mem_limit")

    try:
        memory = int(raw_memory)
    except (TypeError, ValueError):
        fail(f"{service_name} has non-numeric memory limit: {raw_memory!r}")

    if not minimum_bytes <= memory <= maximum_bytes:
        fail(f"{service_name} has unexpected memory limit: {memory}")


for service_name in (
    "app-postgres",
    "keycloak-postgres",
    "redis",
    "kafka",
    "keycloak",
):
    require_no_field(service_name, "ports")

for service_name in (
    "redis",
    "kafka-tls-init",
    "kafka-init",
    "minio-init",
):
    require_no_field(service_name, "build")

for service_name in (
    "app-postgres-security-init",
    "keycloak",
    "keycloak-config",
):
    require_no_field(service_name, "volumes")

expected_images = {
    "app-postgres-security-init": "ghcr.io/knliiss/mnemos-postgres-security-init:0123456789abcdef",
    "redis": "ghcr.io/knliiss/mnemos-redis:0123456789abcdef",
    "kafka-tls-init": "ghcr.io/knliiss/mnemos-kafka-tls-init:0123456789abcdef",
    "kafka-init": "ghcr.io/knliiss/mnemos-kafka-init:0123456789abcdef",
    "minio-init": "ghcr.io/knliiss/mnemos-minio-init:0123456789abcdef",
    "keycloak": "ghcr.io/knliiss/mnemos-keycloak:0123456789abcdef",
    "keycloak-config": "ghcr.io/knliiss/mnemos-keycloak-config:0123456789abcdef",
}

for service_name, expected_image in expected_images.items():
    if services[service_name].get("image") != expected_image:
        fail(f"{service_name} does not use the expected immutable GHCR image")

for setting in (
    "max_connections=120",
    "shared_buffers=1GB",
    "effective_cache_size=8GB",
    "work_mem=4MB",
    "maintenance_work_mem=256MB",
):
    require_command_setting("app-postgres", setting)

for setting in (
    "max_connections=40",
    "shared_buffers=256MB",
    "effective_cache_size=1GB",
    "work_mem=4MB",
    "maintenance_work_mem=128MB",
):
    require_command_setting("keycloak-postgres", setting)

require_memory_limit(
    service_name="app-postgres",
    minimum_bytes=3_000_000_000,
    maximum_bytes=3_300_000_000,
)
require_memory_limit(
    service_name="keycloak-postgres",
    minimum_bytes=1_500_000_000,
    maximum_bytes=1_700_000_000,
)

keycloak_environment = services["keycloak"].get("environment", {})

if keycloak_environment.get("KC_DB_POOL_MAX_SIZE") != "20":
    fail("Keycloak production database pool must be capped at 20 connections")

if keycloak_environment.get("KC_DB_POOL_MIN_SIZE") != "2":
    fail("Keycloak production database pool minimum must be 2 connections")

network = config.get("networks", {}).get("mnemos-network", {})

if network.get("external") is not True:
    fail("Production infrastructure network must be external")

if network.get("name") != "mnemos-production-network":
    fail("Production infrastructure network has an unexpected name")

print("Production infrastructure Compose overlay verified")
PY

sh "$SCRIPT_DIR/verify-production-app-compose.sh"
