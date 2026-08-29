#!/usr/bin/env python3

from __future__ import annotations

import json
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
NORMALIZER = REPOSITORY_ROOT / "scripts" / "normalize-production-swarm-stack.py"
EXPECTED_CLOUDFLARED_COMMAND = (
    "tunnel run --token-file /run/secrets/cloudflare_tunnel_token"
)


def fail(message: str) -> None:
    raise AssertionError(message)


def source_stack() -> dict[str, object]:
    return {
        "version": "3.8",
        "services": {
            "social-service": {
                "image": "busybox:1.36",
                "command": ["unexpected", "compose", "command"],
                "entrypoint": ["/unexpected-entrypoint"],
            },
            "cloudflared": {
                "image": "cloudflare/cloudflared:2026.8.1",
                "command": [
                    "tunnel",
                    "run",
                    "--token-file",
                    "/run/secrets/cloudflare_tunnel_token",
                ],
            },
        },
    }


def normalize() -> dict[str, object]:
    with tempfile.TemporaryDirectory() as directory_name:
        directory = Path(directory_name)
        input_path = directory / "rendered-stack.json"
        output_path = directory / "normalized-stack.json"

        input_path.write_text(json.dumps(source_stack()), encoding="utf-8")

        result = subprocess.run(
            [
                sys.executable,
                str(NORMALIZER),
                "--input",
                str(input_path),
                "--output",
                str(output_path),
            ],
            cwd=REPOSITORY_ROOT,
            check=False,
            capture_output=True,
            text=True,
        )

        if result.returncode != 0:
            fail("Normalizer failed:\n" + result.stdout + result.stderr)

        return json.loads(output_path.read_text(encoding="utf-8"))


def verify_normalized_model(stack: dict[str, object]) -> None:
    services = stack.get("services")

    if not isinstance(services, dict):
        fail("Normalized stack does not contain services")

    social_service = services.get("social-service")

    if not isinstance(social_service, dict):
        fail("social-service is missing from normalized stack")

    if "command" in social_service:
        fail("Application command override was not removed")

    if "entrypoint" in social_service:
        fail("Application entrypoint override was not removed")

    cloudflared = services.get("cloudflared")

    if not isinstance(cloudflared, dict):
        fail("cloudflared is missing from normalized stack")

    if cloudflared.get("command") != EXPECTED_CLOUDFLARED_COMMAND:
        fail("cloudflared command is not normalized to a Swarm-compatible string")


def verify_docker_stack_schema(stack: dict[str, object]) -> None:
    if shutil.which("docker") is None:
        print("Docker unavailable; skipping docker stack config parser verification")
        return

    with tempfile.TemporaryDirectory() as directory_name:
        stack_path = Path(directory_name) / "normalized-stack.json"
        stack_path.write_text(json.dumps(stack), encoding="utf-8")

        result = subprocess.run(
            ["docker", "stack", "config", "--compose-file", str(stack_path)],
            cwd=REPOSITORY_ROOT,
            check=False,
            capture_output=True,
            text=True,
        )

    if result.returncode != 0:
        fail("docker stack config rejected normalized stack:\n" + result.stderr)


def main() -> int:
    stack = normalize()
    verify_normalized_model(stack)
    verify_docker_stack_schema(stack)

    print("Production Swarm stack command normalization verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
