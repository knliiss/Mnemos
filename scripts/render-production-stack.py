#!/usr/bin/env python3

from __future__ import annotations

import argparse
import copy
import json
import os
import sys
from pathlib import Path
from typing import Any

from production_release_manifest import ProductionReleaseManifestError
from production_release_manifest import read_manifest
from production_service_catalog import ProductionServiceCatalogError


APPLICATION_SERVICES = (
    "account-service",
    "bet-service",
    "event-service",
    "gateway-service",
    "moderation-service",
    "notification-service",
    "payment-service",
    "personalization-service",
    "player-directory-service",
    "profile-service",
    "realtime-service",
    "reward-service",
    "social-service",
    "telegram-bot-service",
    "wallet-service",
)

REPLICA_SAFE_SERVICES = frozenset(
    {
        "account-service",
        "event-service",
        "gateway-service",
        "payment-service",
        "wallet-service",
    }
)

SERVICE_MEMORY_LIMITS = {
    "event-service": "768M",
    "gateway-service": "512M",
    "notification-service": "768M",
    "personalization-service": "768M",
    "player-directory-service": "768M",
    "realtime-service": "768M",
}

DEFAULT_MEMORY_LIMIT = "640M"
DEFAULT_MEMORY_RESERVATION = "192M"
CLOUDFLARED_IMAGE = "cloudflare/cloudflared:2026.8.1"
CLOUDFLARED_MEMORY_LIMIT = "256M"
CLOUDFLARED_MEMORY_RESERVATION = "64M"
CLOUDFLARED_SECRET_TARGET = "/run/secrets/cloudflare_tunnel_token"
KAFKA_TRUSTSTORE_TARGET = "/etc/mnemos/kafka/kafka.truststore.p12"
KAFKA_TLS_VOLUME_TARGET = "/etc/mnemos/kafka"


class StackRenderError(RuntimeError):
    pass


def required_environment(name: str) -> str:
    value = os.environ.get(name, "").strip()

    if not value:
        raise StackRenderError(f"{name} is required")

    return value


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Render the resolved Mnemos Compose application model as a production Swarm stack.",
    )
    parser.add_argument(
        "--input",
        type=Path,
        help="Resolved Docker Compose JSON. Reads stdin when omitted.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        help="Destination JSON file. Writes stdout when omitted.",
    )
    parser.add_argument(
        "--release-manifest",
        type=Path,
        help=(
            "Optional per-service release manifest. When omitted, the legacy "
            "MNEMOS_IMAGE_REGISTRY + MNEMOS_RELEASE_TAG image policy is used."
        ),
    )

    return parser.parse_args()


def read_compose_model(input_path: Path | None) -> dict[str, Any]:
    if input_path is None:
        raw = sys.stdin.read()
    else:
        raw = input_path.read_text(encoding="utf-8")

    try:
        model = json.loads(raw)
    except json.JSONDecodeError as exception:
        raise StackRenderError(f"Invalid Compose JSON: {exception}") from exception

    if not isinstance(model, dict):
        raise StackRenderError("Compose model must be a JSON object")

    services = model.get("services")

    if not isinstance(services, dict):
        raise StackRenderError("Compose model does not contain a services object")

    return model


def legacy_application_images() -> dict[str, str]:
    registry = required_environment("MNEMOS_IMAGE_REGISTRY").rstrip("/")
    release_tag = required_environment("MNEMOS_RELEASE_TAG")

    return {
        service_name: f"{registry}/mnemos-{service_name}:{release_tag}"
        for service_name in APPLICATION_SERVICES
    }


def manifest_application_images(path: Path) -> dict[str, str]:
    manifest = read_manifest(path)
    services = manifest["services"]
    images: dict[str, str] = {}

    for service_name in APPLICATION_SERVICES:
        entry = services.get(service_name)

        if not isinstance(entry, dict):
            raise StackRenderError(
                f"Release manifest does not contain application service {service_name}"
            )

        if entry.get("kind") != "application":
            raise StackRenderError(
                f"Release manifest entry {service_name} is not an application image"
            )

        image = entry.get("image")

        if not isinstance(image, str) or not image:
            raise StackRenderError(
                f"Release manifest entry {service_name} does not define an image"
            )

        images[service_name] = image

    return images


def application_images(release_manifest: Path | None) -> dict[str, str]:
    if release_manifest is None:
        return legacy_application_images()

    return manifest_application_images(release_manifest)


def normalized_environment(service: dict[str, Any]) -> dict[str, str]:
    environment = service.get("environment", {})

    if isinstance(environment, dict):
        return {
            str(key): "" if value is None else str(value)
            for key, value in environment.items()
        }

    if isinstance(environment, list):
        normalized: dict[str, str] = {}

        for entry in environment:
            key, separator, value = str(entry).partition("=")
            normalized[key] = value if separator else ""

        return normalized

    raise StackRenderError("Service environment must be an object or a list")


def remove_kafka_tls_volume(service: dict[str, Any]) -> None:
    volumes = service.get("volumes")

    if not isinstance(volumes, list):
        return

    retained: list[Any] = []

    for volume in volumes:
        if isinstance(volume, dict):
            target = volume.get("target")

            if target == KAFKA_TLS_VOLUME_TARGET:
                continue
        elif isinstance(volume, str):
            _, separator, target = volume.partition(":")

            if separator and target.split(":", maxsplit=1)[0] == KAFKA_TLS_VOLUME_TARGET:
                continue

        retained.append(volume)

    if retained:
        service["volumes"] = retained
    else:
        service.pop("volumes", None)


def configure_kafka_truststore(service: dict[str, Any]) -> None:
    remove_kafka_tls_volume(service)

    configs = service.get("configs")

    if configs is None:
        configs = []
        service["configs"] = configs

    if not isinstance(configs, list):
        raise StackRenderError("Service configs must be a list")

    configs.append(
        {
            "source": "kafka_truststore",
            "target": KAFKA_TRUSTSTORE_TARGET,
            "mode": 0o444,
        }
    )


def configure_runtime_environment(
    service_name: str,
    service: dict[str, Any],
) -> None:
    environment = normalized_environment(service)

    if "SERVER_PORT" not in environment:
        raise StackRenderError(f"{service_name} does not define SERVER_PORT")

    environment.setdefault(
        "JAVA_TOOL_OPTIONS",
        "-XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=15.0 -XX:+ExitOnOutOfMemoryError",
    )
    environment["SERVER_SHUTDOWN"] = "graceful"
    environment["SPRING_LIFECYCLE_TIMEOUT_PER_SHUTDOWN_PHASE"] = "30s"
    environment["MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED"] = "true"

    service["environment"] = environment


def configure_healthcheck(
    service_name: str,
    service: dict[str, Any],
) -> None:
    environment = normalized_environment(service)
    server_port = environment["SERVER_PORT"]

    service["healthcheck"] = {
        "test": [
            "CMD-SHELL",
            (
                f"wget -q -O - http://127.0.0.1:{server_port}/actuator/health/readiness "
                "| grep -q '\"status\":\"UP\"'"
            ),
        ],
        "interval": "10s",
        "timeout": "5s",
        "retries": 6,
        "start_period": "60s",
    }


def application_deployment_policy(service_name: str) -> dict[str, Any]:
    replica_safe = service_name in REPLICA_SAFE_SERVICES
    replicas = 2 if replica_safe else 1
    update_order = "start-first" if replica_safe else "stop-first"
    memory_limit = SERVICE_MEMORY_LIMITS.get(service_name, DEFAULT_MEMORY_LIMIT)

    return {
        "mode": "replicated",
        "replicas": replicas,
        "endpoint_mode": "vip",
        "update_config": {
            "parallelism": 1,
            "delay": "5s",
            "monitor": "45s",
            "failure_action": "rollback",
            "order": update_order,
        },
        "rollback_config": {
            "parallelism": 1,
            "delay": "5s",
            "monitor": "45s",
            "failure_action": "pause",
            "order": update_order,
        },
        "restart_policy": {
            "condition": "any",
            "delay": "5s",
            "window": "60s",
        },
        "resources": {
            "limits": {
                "memory": memory_limit,
            },
            "reservations": {
                "memory": DEFAULT_MEMORY_RESERVATION,
            },
        },
    }


def configure_deployment(
    service_name: str,
    service: dict[str, Any],
) -> None:
    service["stop_grace_period"] = "35s"
    service["deploy"] = application_deployment_policy(service_name)


def sanitize_service(
    service_name: str,
    source: dict[str, Any],
    image: str,
) -> dict[str, Any]:
    service = copy.deepcopy(source)

    for unsupported_field in (
        "build",
        "container_name",
        "depends_on",
        "develop",
        "profiles",
        "pull_policy",
        "restart",
    ):
        service.pop(unsupported_field, None)

    service.pop("ports", None)
    service["image"] = image

    configure_runtime_environment(
        service_name=service_name,
        service=service,
    )
    configure_healthcheck(
        service_name=service_name,
        service=service,
    )
    configure_kafka_truststore(service)
    configure_deployment(
        service_name=service_name,
        service=service,
    )

    service["networks"] = ["mnemos-network"]

    return service


def cloudflared_service() -> dict[str, Any]:
    return {
        "image": CLOUDFLARED_IMAGE,
        "command": [
            "tunnel",
            "run",
            "--token-file",
            CLOUDFLARED_SECRET_TARGET,
        ],
        "user": "65532:65532",
        "read_only": True,
        "cap_drop": ["ALL"],
        "security_opt": ["no-new-privileges:true"],
        "stop_grace_period": "20s",
        "networks": ["mnemos-network"],
        "secrets": [
            {
                "source": "cloudflare_tunnel_token",
                "target": CLOUDFLARED_SECRET_TARGET,
                "uid": "65532",
                "gid": "65532",
                "mode": 0o400,
            }
        ],
        "deploy": {
            "mode": "replicated",
            "replicas": 2,
            "endpoint_mode": "vip",
            "update_config": {
                "parallelism": 1,
                "delay": "5s",
                "monitor": "30s",
                "failure_action": "rollback",
                "order": "start-first",
            },
            "rollback_config": {
                "parallelism": 1,
                "delay": "5s",
                "monitor": "30s",
                "failure_action": "pause",
                "order": "start-first",
            },
            "restart_policy": {
                "condition": "any",
                "delay": "5s",
                "window": "60s",
            },
            "resources": {
                "limits": {
                    "memory": CLOUDFLARED_MEMORY_LIMIT,
                },
                "reservations": {
                    "memory": CLOUDFLARED_MEMORY_RESERVATION,
                },
            },
        },
    }


def render_stack(
    model: dict[str, Any],
    release_manifest: Path | None = None,
) -> dict[str, Any]:
    images = application_images(release_manifest)
    swarm_network = os.environ.get(
        "MNEMOS_SWARM_NETWORK",
        "mnemos-production-network",
    ).strip()
    truststore_config = required_environment("MNEMOS_KAFKA_TRUSTSTORE_CONFIG")
    cloudflare_tunnel_secret = required_environment("MNEMOS_CLOUDFLARE_TUNNEL_SECRET")

    source_services = model["services"]
    missing_services = [
        service_name
        for service_name in APPLICATION_SERVICES
        if service_name not in source_services
    ]

    if missing_services:
        raise StackRenderError(
            "Compose model is missing application services: "
            + ", ".join(missing_services)
        )

    rendered_services = {
        service_name: sanitize_service(
            service_name=service_name,
            source=source_services[service_name],
            image=images[service_name],
        )
        for service_name in APPLICATION_SERVICES
    }
    rendered_services["cloudflared"] = cloudflared_service()

    return {
        "version": "3.8",
        "services": rendered_services,
        "networks": {
            "mnemos-network": {
                "external": True,
                "name": swarm_network,
            }
        },
        "configs": {
            "kafka_truststore": {
                "external": True,
                "name": truststore_config,
            }
        },
        "secrets": {
            "cloudflare_tunnel_token": {
                "external": True,
                "name": cloudflare_tunnel_secret,
            }
        },
    }


def write_stack(
    stack: dict[str, Any],
    output_path: Path | None,
) -> None:
    serialized = json.dumps(
        stack,
        indent=2,
        sort_keys=False,
    )
    serialized += "\n"

    if output_path is None:
        sys.stdout.write(serialized)
    else:
        output_path.write_text(serialized, encoding="utf-8")


def main() -> int:
    arguments = parse_arguments()

    try:
        model = read_compose_model(arguments.input)
        stack = render_stack(
            model=model,
            release_manifest=arguments.release_manifest,
        )
        write_stack(
            stack=stack,
            output_path=arguments.output,
        )
    except (
        ProductionReleaseManifestError,
        ProductionServiceCatalogError,
        StackRenderError,
    ) as exception:
        print(f"Production stack render failed: {exception}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
