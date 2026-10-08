#!/usr/bin/env python3

from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
from pathlib import Path

from production_release_manifest import create_manifest
from production_release_manifest import update_manifest


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
RENDERER = REPOSITORY_ROOT / "scripts" / "render-production-stack.py"

APPLICATION_PORTS = {
    "account-service": 8081,
    "wallet-service": 8082,
    "event-service": 8083,
    "notification-service": 8084,
    "reward-service": 8085,
    "bet-service": 8086,
    "bot-management-service": 8095,
    "moderation-service": 8087,
    "realtime-service": 8088,
    "personalization-service": 8089,
    "social-service": 8090,
    "profile-service": 8091,
    "player-directory-service": 8092,
    "payment-service": 8093,
    "telegram-bot-service": 8094,
    "gateway-service": 8080,
}

BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
REGISTRY = "ghcr.io/knliiss"


def fail(message: str) -> None:
    raise AssertionError(message)


def source_model() -> dict[str, object]:
    services: dict[str, object] = {}

    for service_name, port in APPLICATION_PORTS.items():
        services[service_name] = {
            "environment": {
                "SERVER_PORT": str(port),
            },
            "image": f"mnemos/{service_name}:local",
        }

    return {
        "services": services,
    }


def expected_image(service_name: str) -> str:
    source_sha = ACCOUNT_SHA if service_name == "account-service" else BASE_SHA

    return f"{REGISTRY}/mnemos-{service_name}:{source_sha}"


def render_with_manifest() -> dict[str, object]:
    base_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target_manifest = update_manifest(
        base=base_manifest,
        assignments=[("account-service", ACCOUNT_SHA)],
    )

    environment = os.environ.copy()
    environment.pop("MNEMOS_IMAGE_REGISTRY", None)
    environment.pop("MNEMOS_RELEASE_TAG", None)
    environment.update(
        {
            "MNEMOS_SWARM_NETWORK": "mnemos-production-network",
            "MNEMOS_KAFKA_TRUSTSTORE_CONFIG": "mnemos-kafka-truststore-deadbeef",
            "MNEMOS_CLOUDFLARE_TUNNEL_SECRET": "mnemos-cloudflare-tunnel-token-deadbeef",
        }
    )

    with tempfile.TemporaryDirectory() as directory:
        directory_path = Path(directory)
        input_path = directory_path / "compose.json"
        manifest_path = directory_path / "release-manifest.json"
        output_path = directory_path / "stack.json"

        input_path.write_text(
            json.dumps(source_model()),
            encoding="utf-8",
        )
        manifest_path.write_text(
            json.dumps(target_manifest),
            encoding="utf-8",
        )

        result = subprocess.run(
            [
                sys.executable,
                str(RENDERER),
                "--input",
                str(input_path),
                "--output",
                str(output_path),
                "--release-manifest",
                str(manifest_path),
            ],
            cwd=REPOSITORY_ROOT,
            env=environment,
            check=False,
            capture_output=True,
            text=True,
        )

        if result.returncode != 0:
            fail("Manifest renderer failed:\n" + result.stdout + result.stderr)

        return json.loads(output_path.read_text(encoding="utf-8"))


def main() -> int:
    stack = render_with_manifest()
    services = stack.get("services")

    if not isinstance(services, dict):
        fail("Rendered manifest stack does not contain services")

    for service_name in APPLICATION_PORTS:
        service = services.get(service_name)

        if not isinstance(service, dict):
            fail(f"Rendered manifest stack is missing {service_name}")

        image = service.get("image")

        if image != expected_image(service_name):
            fail(
                f"{service_name} image does not match its release manifest: {image}"
            )

    account_image = services["account-service"]["image"]
    wallet_image = services["wallet-service"]["image"]

    if account_image == wallet_image:
        fail("Per-service manifest images were collapsed to one release image")

    print("Production per-service manifest stack rendering verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production per-service manifest stack verification failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
