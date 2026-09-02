#!/usr/bin/env python3

from __future__ import annotations

import json
import subprocess
import sys
import tempfile
from pathlib import Path

import production_deployment_plan
import production_deployment_executor
import production_runtime_policy
import reconcile_production_runtime_policy_placeholder


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
NORMALIZER = REPOSITORY_ROOT / "scripts" / "normalize-production-swarm-stack.py"
RECONCILER = REPOSITORY_ROOT / "scripts" / "reconcile-production-runtime-policy.py"
OVERRIDES = REPOSITORY_ROOT / "infrastructure" / "production" / "compose.app-overrides.yml"

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


def verify_classification() -> None:
    application_services = set(production_runtime_policy.APPLICATION_SERVICES)

    if set(production_runtime_policy.ACTIVE_ACTIVE_SERVICES) != EXPECTED_ACTIVE_ACTIVE:
        fail("Active-active production service classification changed unexpectedly")

    expected_singletons = application_services - EXPECTED_ACTIVE_ACTIVE

    if set(production_runtime_policy.SINGLETON_SERVICES) != expected_singletons:
        fail("Singleton production service classification changed unexpectedly")

    for service_name in EXPECTED_ACTIVE_ACTIVE:
        if production_runtime_policy.desired_replicas(service_name) != 2:
            fail(f"{service_name} must run with two production replicas")

        if production_runtime_policy.update_order(service_name) != "start-first":
            fail(f"{service_name} must use start-first production updates")

    for service_name in expected_singletons:
        if production_runtime_policy.desired_replicas(service_name) != 1:
            fail(f"{service_name} must remain a singleton")

        if production_runtime_policy.update_order(service_name) != "stop-first":
            fail(f"{service_name} must retain singleton-safe stop-first updates")


def verify_priorities() -> None:
    priority = production_runtime_policy.deployment_priority

    if priority("gateway-service") <= priority("moderation-service"):
        fail("Gateway must deploy after moderation and other upstream services")

    if priority("gateway-service") <= priority("realtime-service"):
        fail("Gateway must deploy after realtime-service")

    if priority("telegram-bot-service") <= priority("gateway-service"):
        fail("Telegram bot singleton must deploy last")


def verify_moderation_pool() -> None:
    overrides = production_runtime_policy.runtime_environment_overrides(
        "moderation-service"
    )

    if overrides.get("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE") != "4":
        fail("Moderation runtime Hikari pool override must be 4")

    text = OVERRIDES.read_text(encoding="utf-8")
    marker = "  moderation-service:\n    environment:\n      <<: *production-datasource-defaults\n      SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: \"4\""

    if marker not in text:
        fail("Production Compose moderation Hikari pool is not aligned to 4")


def verify_normalizer() -> None:
    services = {
        service_name: {
            "image": f"example.invalid/{service_name}:unchanged",
            "deploy": {
                "resources": {
                    "limits": {"memory": "640M"},
                    "reservations": {"memory": "192M"},
                }
            },
        }
        for service_name in production_runtime_policy.APPLICATION_SERVICES
    }
    services["cloudflared"] = {"image": "cloudflare/cloudflared:test"}

    with tempfile.TemporaryDirectory() as directory:
        input_path = Path(directory) / "input.json"
        output_path = Path(directory) / "output.json"
        input_path.write_text(
            json.dumps({"services": services}),
            encoding="utf-8",
        )

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
            fail("Swarm normalizer failed:\n" + result.stdout + result.stderr)

        normalized = json.loads(output_path.read_text(encoding="utf-8"))

    for service_name in production_runtime_policy.APPLICATION_SERVICES:
        service = normalized["services"][service_name]
        deploy = service["deploy"]
        expected_replicas = production_runtime_policy.desired_replicas(service_name)
        expected_order = production_runtime_policy.update_order(service_name)

        if service["image"] != f"example.invalid/{service_name}:unchanged":
            fail(f"Normalizer must not change {service_name} image")

        if deploy.get("replicas") != expected_replicas:
            fail(f"Normalizer replica policy mismatch for {service_name}")

        if deploy.get("update_config", {}).get("order") != expected_order:
            fail(f"Normalizer update order mismatch for {service_name}")

        if deploy.get("rollback_config", {}).get("order") != expected_order:
            fail(f"Normalizer rollback order mismatch for {service_name}")


def load_reconciler_module():
    import importlib.util

    specification = importlib.util.spec_from_file_location(
        "reconcile_production_runtime_policy",
        RECONCILER,
    )

    if specification is None or specification.loader is None:
        fail("Could not load production runtime policy reconciler")

    module = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(module)
    return module


def verify_reconciler_commands() -> None:
    reconciler = load_reconciler_module()
    moderation = reconciler.update_command(
        "moderation-service",
        "mnemos_moderation-service",
    )

    if "--image" in moderation:
        fail("Runtime policy reconciliation must never replace service images")

    if moderation[moderation.index("--replicas") + 1] != "2":
        fail("Moderation policy reconciliation must scale to two replicas")

    if moderation[moderation.index("--update-order") + 1] != "start-first":
        fail("Moderation policy reconciliation must use start-first")

    if "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=4" not in moderation:
        fail("Moderation policy reconciliation must apply Hikari pool=4")

    telegram = reconciler.update_command(
        "telegram-bot-service",
        "mnemos_telegram-bot-service",
    )

    if telegram[telegram.index("--replicas") + 1] != "1":
        fail("Telegram bot must remain a singleton")

    if telegram[telegram.index("--update-order") + 1] != "stop-first":
        fail("Telegram bot singleton must not overlap pollers during an update")


def verify_executor_guard() -> None:
    commands = production_deployment_executor.availability_steps()

    if len(commands) != 1:
        fail("Scoped production executor must define one availability gate")

    command = commands[0].get("command", [])

    if not command or not str(command[-1]).endswith("verify-production-availability.sh"):
        fail("Scoped production executor does not call the availability guard")


def verify_plan_order() -> None:
    catalog = {
        "services": {
            service_name: {"deploy": {"runtime": "swarm"}}
            for service_name in production_runtime_policy.APPLICATION_SERVICES
        }
    }

    ordered = sorted(
        production_runtime_policy.APPLICATION_SERVICES,
        key=lambda service_name: production_deployment_plan.action_order(
            service_name,
            catalog,
        ),
    )

    if ordered[-1] != "telegram-bot-service":
        fail("Telegram bot must be the final scoped deployment action")

    if ordered[-2] != "gateway-service":
        fail("Gateway must be the final HTTP application deployment action")


def main() -> int:
    try:
        verify_classification()
        verify_priorities()
        verify_moderation_pool()
        verify_normalizer()
        verify_reconciler_commands()
        verify_executor_guard()
        verify_plan_order()
    except (AssertionError, OSError, ValueError) as exception:
        print(f"Production runtime policy verification failed: {exception}", file=sys.stderr)
        return 1

    print("Production zero-downtime runtime policy verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
