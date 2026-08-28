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


def memory_bytes(service_name: str) -> int:
    raw_memory = services[service_name].get("mem_limit")

    try:
        return int(raw_memory)
    except (TypeError, ValueError):
        fail(f"{service_name} has non-numeric memory limit: {raw_memory!r}")


def require_memory_limit(service_name: str, minimum_bytes: int, maximum_bytes: int) -> None:
    memory = memory_bytes(service_name)

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

require_memory_limit("app-postgres", 2_600_000_000, 2_800_000_000)
require_memory_limit("keycloak-postgres", 1_000_000_000, 1_200_000_000)
require_memory_limit("redis", 750_000_000, 850_000_000)
require_memory_limit("kafka", 1_500_000_000, 1_700_000_000)
require_memory_limit("minio", 750_000_000, 850_000_000)
require_memory_limit("keycloak", 1_300_000_000, 1_400_000_000)

infrastructure_limit = sum(
    memory_bytes(service_name)
    for service_name in (
        "app-postgres",
        "keycloak-postgres",
        "redis",
        "kafka",
        "minio",
        "keycloak",
    )
)

if infrastructure_limit > 8_500_000_000:
    fail(
        "Production infrastructure hard memory limits exceed the host budget: "
        f"{infrastructure_limit} bytes"
    )

kafka_environment = services["kafka"].get("environment", {})

if kafka_environment.get("KAFKA_HEAP_OPTS") != "-Xms512m -Xmx768m":
    fail("Kafka production heap must be explicitly bounded")

keycloak_environment = services["keycloak"].get("environment", {})

if keycloak_environment.get("KC_DB_POOL_MAX_SIZE") != "20":
    fail("Keycloak production database pool must be capped at 20 connections")

if keycloak_environment.get("KC_DB_POOL_MIN_SIZE") != "2":
    fail("Keycloak production database pool minimum must be 2 connections")

if keycloak_environment.get("JAVA_OPTS_KC_HEAP") != "-XX:MaxRAMPercentage=65.0 -XX:InitialRAMPercentage=20.0":
    fail("Keycloak production heap policy must be explicitly bounded")

network = config.get("networks", {}).get("mnemos-network", {})

if network.get("external") is not True:
    fail("Production infrastructure network must be external")

if network.get("name") != "mnemos-production-network":
    fail("Production infrastructure network has an unexpected name")

print("Production infrastructure Compose overlay verified")
PY

sh "$SCRIPT_DIR/verify-production-app-compose.sh"
