#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

from production_runtime_policy import APPLICATION_SERVICES
from production_runtime_policy import desired_replicas
from production_runtime_policy import update_order


APPLICATION_TMPFS_TARGET = "/tmp"
APPLICATION_TMPFS_SIZE_BYTES = 64 * 1024 * 1024
APPLICATION_TMPFS_MODE = 0o1777
CLOUDFLARED_SERVICE = "cloudflared"
CLOUDFLARED_COMMAND = (
    "tunnel --protocol http2 run --token-file /run/secrets/cloudflare_tunnel_token"
)


class SwarmStackNormalizationError(RuntimeError):
    pass


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Normalize the rendered Mnemos production stack for Docker Swarm's "
            "legacy Compose schema."
        ),
    )
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)

    return parser.parse_args()


def read_stack(path: Path) -> dict[str, Any]:
    try:
        stack = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exception:
        raise SwarmStackNormalizationError(
            f"Invalid rendered stack JSON: {exception}"
        ) from exception

    if not isinstance(stack, dict):
        raise SwarmStackNormalizationError("Rendered stack must be a JSON object")

    services = stack.get("services")

    if not isinstance(services, dict):
        raise SwarmStackNormalizationError(
            "Rendered stack does not contain a services object"
        )

    return stack


def volume_target(volume: Any) -> str | None:
    if isinstance(volume, dict):
        target = volume.get("target")
        return target if isinstance(target, str) else None

    if isinstance(volume, str):
        _, separator, target = volume.partition(":")

        if not separator:
            return volume

        return target.split(":", maxsplit=1)[0]

    return None


def configure_application_tmpfs(service: dict[str, Any]) -> None:
    service.pop("tmpfs", None)

    volumes = service.get("volumes")

    if volumes is None:
        volumes = []
    elif not isinstance(volumes, list):
        raise SwarmStackNormalizationError(
            "Application service volumes must be a list"
        )

    retained_volumes = [
        volume
        for volume in volumes
        if volume_target(volume) != APPLICATION_TMPFS_TARGET
    ]
    retained_volumes.append(
        {
            "type": "tmpfs",
            "target": APPLICATION_TMPFS_TARGET,
            "tmpfs": {
                "size": APPLICATION_TMPFS_SIZE_BYTES,
                "mode": APPLICATION_TMPFS_MODE,
            },
        }
    )

    service["volumes"] = retained_volumes


def configure_application_deployment(
    service_name: str,
    service: dict[str, Any],
) -> None:
    deploy = service.get("deploy")

    if deploy is None:
        deploy = {}
    elif not isinstance(deploy, dict):
        raise SwarmStackNormalizationError(
            f"{service_name} deploy policy must be an object"
        )

    order = update_order(service_name)

    deploy["mode"] = "replicated"
    deploy["replicas"] = desired_replicas(service_name)
    deploy["update_config"] = {
        "parallelism": 1,
        "delay": "5s",
        "monitor": "45s",
        "failure_action": "rollback",
        "order": order,
    }
    deploy["rollback_config"] = {
        "parallelism": 1,
        "delay": "5s",
        "monitor": "45s",
        "failure_action": "pause",
        "order": order,
    }

    service["deploy"] = deploy
    service.pop("endpoint_mode", None)


def normalize_application_service(
    service_name: str,
    service: dict[str, Any],
) -> None:
    service.pop("command", None)
    service.pop("entrypoint", None)

    configure_application_tmpfs(service)
    configure_application_deployment(
        service_name=service_name,
        service=service,
    )


def normalize_cloudflared_service(service: dict[str, Any]) -> None:
    service["command"] = CLOUDFLARED_COMMAND


def normalize_stack(stack: dict[str, Any]) -> dict[str, Any]:
    services = stack["services"]

    for service_name in APPLICATION_SERVICES:
        service = services.get(service_name)

        if service is None:
            continue

        if not isinstance(service, dict):
            raise SwarmStackNormalizationError(
                f"{service_name} must be a service object"
            )

        normalize_application_service(
            service_name=service_name,
            service=service,
        )

    cloudflared = services.get(CLOUDFLARED_SERVICE)

    if cloudflared is not None:
        if not isinstance(cloudflared, dict):
            raise SwarmStackNormalizationError(
                f"{CLOUDFLARED_SERVICE} must be a service object"
            )

        normalize_cloudflared_service(cloudflared)

    return stack


def write_stack(stack: dict[str, Any], path: Path) -> None:
    path.write_text(
        json.dumps(stack, indent=2, sort_keys=False) + "\n",
        encoding="utf-8",
    )


def main() -> int:
    arguments = parse_arguments()

    try:
        stack = read_stack(arguments.input)
        normalized = normalize_stack(stack)
        write_stack(normalized, arguments.output)
    except (SwarmStackNormalizationError, ValueError) as exception:
        print(f"Production Swarm normalization failed: {exception}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
