#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


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
    "wallet-service",
)

CLOUDFLARED_SERVICE = "cloudflared"
CLOUDFLARED_COMMAND = (
    "tunnel run --token-file /run/secrets/cloudflare_tunnel_token"
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


def normalize_application_service(service: dict[str, Any]) -> None:
    service.pop("command", None)
    service.pop("entrypoint", None)


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

        normalize_application_service(service)

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
    except SwarmStackNormalizationError as exception:
        print(f"Production Swarm normalization failed: {exception}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
