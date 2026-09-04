#!/usr/bin/env python3

from __future__ import annotations

import importlib.util
import json
import subprocess
import sys
import tempfile
from pathlib import Path

import production_deployment_executor
import production_deployment_plan
import production_runtime_policy


ROOT = Path(__file__).resolve().parent.parent
NORMALIZER = ROOT / "scripts" / "normalize-production-swarm-stack.py"
RECONCILER = ROOT / "scripts" / "reconcile-production-runtime-policy.py"
OVERRIDES = ROOT / "infrastructure" / "production" / "compose.app-overrides.yml"
RUNTIME_POLICY_WORKFLOW = ROOT / ".github" / "workflows" / "production-runtime-policy.yml"

EXPECTED_ACTIVE_ACTIVE = {
    "account-service",
    "event-service",
    "gateway-service",
    "moderation-service",
    "payment-service",
    "personalization-service",
    "player-directory-service",
    "wallet-service",
}


def fail(message: str) -> None:
    raise AssertionError(message)


def load_reconciler():
    specification = importlib.util.spec_from_file_location(
        "production_runtime_policy_reconciler",
        RECONCILER,
    )

    if specification is None or specification.loader is None:
        fail("Could not load production runtime policy reconciler")

    module = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(module)
    return module


def verify_classification() -> None:
    services = set(production_runtime_policy.APPLICATION_SERVICES)

    if set(production_runtime_policy.ACTIVE_ACTIVE_SERVICES) != EXPECTED_ACTIVE_ACTIVE:
        fail("Unexpected active-active production service classification")

    if set(production_runtime_policy.SINGLETON_SERVICES) != services - EXPECTED_ACTIVE_ACTIVE:
        fail("Unexpected singleton production service classification")

    for service_name in services:
        active_active = service_name in EXPECTED_ACTIVE_ACTIVE
        expected_replicas = 2 if active_active else 1
        expected_order = "start-first" if active_active else "stop-first"

        if production_runtime_policy.desired_replicas(service_name) != expected_replicas:
            fail(f"Unexpected replica policy for {service_name}")

        if production_runtime_policy.update_order(service_name) != expected_order:
            fail(f"Unexpected update order for {service_name}")


def verify_ordering() -> None:
    actions = [
        {
            "service": service_name,
            "runtime": {"type": "swarm"},
        }
        for service_name in production_runtime_policy.APPLICATION_SERVICES
    ]
    ordered = [
        action["service"]
        for action in sorted(
            actions,
            key=production_deployment_plan.action_order,
        )
    ]

    if ordered[-2:] != ["gateway-service", "telegram-bot-service"]:
        fail("Gateway and Telegram bot must be the final scoped deployment actions")

    compose_action = {
        "service": "postgres",
        "runtime": {"type": "compose"},
    }

    if production_deployment_plan.action_order(compose_action)[0] != 0:
        fail("Compose runtime actions must remain before Swarm application actions")


def verify_moderation_pool() -> None:
    runtime_value = production_runtime_policy.runtime_environment_overrides(
        "moderation-service"
    ).get("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE")

    if runtime_value != "4":
        fail("Moderation runtime Hikari pool must be 4")

    expected = (
        "  moderation-service:\n"
        "    environment:\n"
        "      <<: *production-datasource-defaults\n"
        "      SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: \"4\""
    )

    if expected not in OVERRIDES.read_text(encoding="utf-8"):
        fail("Production Compose moderation Hikari pool must be 4")


def verify_normalizer() -> None:
    services = {
        name: {
            "image": f"example.invalid/{name}:unchanged",
            "deploy": {
                "resources": {
                    "limits": {"memory": "640M"},
                    "reservations": {"memory": "192M"},
                }
            },
        }
        for name in production_runtime_policy.APPLICATION_SERVICES
    }

    with tempfile.TemporaryDirectory() as directory:
        source = Path(directory) / "source.json"
        target = Path(directory) / "target.json"
        source.write_text(json.dumps({"services": services}), encoding="utf-8")

        result = subprocess.run(
            [
                sys.executable,
                str(NORMALIZER),
                "--input",
                str(source),
                "--output",
                str(target),
            ],
            cwd=ROOT,
            check=False,
            capture_output=True,
            text=True,
        )

        if result.returncode != 0:
            fail(result.stdout + result.stderr)

        normalized = json.loads(target.read_text(encoding="utf-8"))

    for name in production_runtime_policy.APPLICATION_SERVICES:
        service = normalized["services"][name]
        deploy = service["deploy"]

        if service["image"] != f"example.invalid/{name}:unchanged":
            fail(f"Normalizer changed the image for {name}")

        if deploy["replicas"] != production_runtime_policy.desired_replicas(name):
            fail(f"Normalizer replica policy mismatch for {name}")

        if deploy["update_config"]["order"] != production_runtime_policy.update_order(name):
            fail(f"Normalizer update policy mismatch for {name}")


def verify_reconciler() -> None:
    reconciler = load_reconciler()
    moderation = reconciler.update_command(
        "moderation-service",
        "mnemos_moderation-service",
    )

    if "--image" in moderation:
        fail("Runtime policy reconciliation must not replace images")

    if moderation[moderation.index("--replicas") + 1] != "2":
        fail("Moderation runtime policy must use two replicas")

    if moderation[moderation.index("--update-order") + 1] != "start-first":
        fail("Moderation runtime policy must use start-first")

    if "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=4" not in moderation:
        fail("Moderation runtime policy must apply Hikari pool=4")

    telegram = reconciler.update_command(
        "telegram-bot-service",
        "mnemos_telegram-bot-service",
    )

    if telegram[telegram.index("--replicas") + 1] != "1":
        fail("Telegram bot must remain a singleton")

    if telegram[telegram.index("--update-order") + 1] != "stop-first":
        fail("Telegram bot updates must not overlap pollers")

    cloudflared = reconciler.cloudflared_update_command("mnemos_cloudflared")

    if "--image" in cloudflared:
        fail("Cloudflared runtime reconciliation must not replace the image")

    if cloudflared[cloudflared.index("--args") + 1] != reconciler.CLOUDFLARED_COMMAND:
        fail("Cloudflared runtime reconciliation must apply the HTTP/2 command")

    current_cloudflared = {
        "TaskTemplate": {
            "ContainerSpec": {
                "Args": reconciler.CLOUDFLARED_COMMAND.split(),
            }
        }
    }
    legacy_cloudflared = {
        "TaskTemplate": {
            "ContainerSpec": {
                "Args": [
                    "tunnel",
                    "run",
                    "--token-file",
                    "/run/secrets/cloudflare_tunnel_token",
                ],
            }
        }
    }

    if not reconciler.cloudflared_policy_matches(current_cloudflared):
        fail("Cloudflared HTTP/2 runtime must be recognized as reconciled")

    if reconciler.cloudflared_policy_matches(legacy_cloudflared):
        fail("Legacy Cloudflare Tunnel transport must require reconciliation")

    if reconciler.CLOUDFLARED_COMMAND not in NORMALIZER.read_text(encoding="utf-8"):
        fail("Normalizer and runtime reconciler disagree on the cloudflared command")


def verify_workflow_state_recording() -> None:
    workflow = RUNTIME_POLICY_WORKFLOW.read_text(encoding="utf-8")
    root_owned_temp = 'state_tmp="$(sudo -n mktemp)"'
    runner_owned_temp = 'state_tmp="$(mktemp)"'

    if root_owned_temp not in workflow:
        fail("Production runtime state recording must create its temp file as root")

    if runner_owned_temp in workflow:
        fail("Production runtime state recording must not pass a runner-owned /tmp file to root")


def verify_executor() -> None:
    steps = production_deployment_executor.availability_steps()

    if len(steps) != 1:
        fail("Scoped executor must define exactly one availability guard step")

    command = steps[0].get("command", [])

    if not command or not str(command[-1]).endswith("verify-production-availability.sh"):
        fail("Scoped executor does not use the production availability guard")


def main() -> int:
    try:
        verify_classification()
        verify_ordering()
        verify_moderation_pool()
        verify_normalizer()
        verify_reconciler()
        verify_workflow_state_recording()
        verify_executor()
    except (AssertionError, OSError, ValueError) as exception:
        print(f"Production runtime policy verification failed: {exception}", file=sys.stderr)
        return 1

    print("Production zero-downtime runtime policy verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
