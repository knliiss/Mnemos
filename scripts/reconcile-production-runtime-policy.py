#!/usr/bin/env python3

from __future__ import annotations

import json
import os
import subprocess
import sys
from pathlib import Path
from typing import Any

from production_runtime_policy import APPLICATION_SERVICES
from production_runtime_policy import desired_replicas
from production_runtime_policy import update_order


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
STACK_NAME = os.environ.get("MNEMOS_SWARM_STACK_NAME", "mnemos").strip() or "mnemos"
WAIT_SCRIPT = REPOSITORY_ROOT / "scripts" / "wait-production-service.sh"
AVAILABILITY_SCRIPT = REPOSITORY_ROOT / "scripts" / "verify-production-availability.sh"


class ProductionRuntimePolicyError(RuntimeError):
    pass


def require_root() -> None:
    if os.geteuid() != 0:
        raise ProductionRuntimePolicyError(
            "Production runtime policy reconciliation must run as root"
        )


def require_swarm_manager() -> None:
    state = subprocess.run(
        [
            "docker",
            "info",
            "--format",
            "{{.Swarm.LocalNodeState}} {{.Swarm.ControlAvailable}}",
        ],
        check=True,
        capture_output=True,
        text=True,
    ).stdout.strip()

    if state != "active true":
        raise ProductionRuntimePolicyError(
            f"Production policy reconciliation requires an active Swarm manager: {state}"
        )


def inspect_service(runtime_name: str) -> dict[str, Any]:
    result = subprocess.run(
        [
            "docker",
            "service",
            "inspect",
            "--format",
            "{{json .Spec}}",
            runtime_name,
        ],
        check=True,
        capture_output=True,
        text=True,
    )

    try:
        specification = json.loads(result.stdout)
    except json.JSONDecodeError as exception:
        raise ProductionRuntimePolicyError(
            f"Could not decode Swarm service specification for {runtime_name}"
        ) from exception

    if not isinstance(specification, dict):
        raise ProductionRuntimePolicyError(
            f"Invalid Swarm service specification for {runtime_name}"
        )

    return specification


def service_image(specification: dict[str, Any], runtime_name: str) -> str:
    try:
        image = specification["TaskTemplate"]["ContainerSpec"]["Image"]
    except (KeyError, TypeError) as exception:
        raise ProductionRuntimePolicyError(
            f"Swarm service has no container image: {runtime_name}"
        ) from exception

    if not isinstance(image, str) or not image:
        raise ProductionRuntimePolicyError(
            f"Swarm service image is invalid: {runtime_name}"
        )

    return image


def current_replicas(specification: dict[str, Any]) -> int | None:
    try:
        replicas = specification["Mode"]["Replicated"]["Replicas"]
    except (KeyError, TypeError):
        return None

    return replicas if isinstance(replicas, int) else None


def current_order(
    specification: dict[str, Any],
    policy_name: str,
) -> str | None:
    policy = specification.get(policy_name)

    if not isinstance(policy, dict):
        return None

    order = policy.get("Order")
    return order if isinstance(order, str) else None


def policy_matches(
    service_name: str,
    specification: dict[str, Any],
) -> bool:
    expected_replicas = desired_replicas(service_name)
    expected_order = update_order(service_name)

    return (
        current_replicas(specification) == expected_replicas
        and current_order(specification, "UpdateConfig") == expected_order
        and current_order(specification, "RollbackConfig") == expected_order
    )


def run_availability_guard() -> None:
    subprocess.run(
        ["sh", str(AVAILABILITY_SCRIPT)],
        cwd=REPOSITORY_ROOT,
        check=True,
    )


def reconcile_service(service_name: str) -> bool:
    runtime_name = f"{STACK_NAME}_{service_name}"
    before = inspect_service(runtime_name)
    image = service_image(before, runtime_name)

    if policy_matches(service_name, before):
        print(f"Production runtime policy already current: {service_name}")
        return False

    order = update_order(service_name)
    replicas = desired_replicas(service_name)

    print(
        "Reconciling production runtime policy: "
        f"{service_name} replicas={replicas} order={order}"
    )

    subprocess.run(
        [
            "docker",
            "service",
            "update",
            "--detach=true",
            "--replicas",
            str(replicas),
            "--update-parallelism",
            "1",
            "--update-delay",
            "5s",
            "--update-monitor",
            "45s",
            "--update-failure-action",
            "rollback",
            "--update-order",
            order,
            "--rollback-parallelism",
            "1",
            "--rollback-delay",
            "5s",
            "--rollback-monitor",
            "45s",
            "--rollback-failure-action",
            "pause",
            "--rollback-order",
            order,
            runtime_name,
        ],
        cwd=REPOSITORY_ROOT,
        check=True,
    )

    subprocess.run(
        [
            "sh",
            str(WAIT_SCRIPT),
            runtime_name,
            image,
            "300",
            "15",
        ],
        cwd=REPOSITORY_ROOT,
        check=True,
    )

    after = inspect_service(runtime_name)
    after_image = service_image(after, runtime_name)

    if after_image != image:
        raise ProductionRuntimePolicyError(
            f"Runtime policy reconciliation changed the image for {service_name}: "
            f"before={image} after={after_image}"
        )

    if not policy_matches(service_name, after):
        raise ProductionRuntimePolicyError(
            f"Runtime policy did not converge for {service_name}"
        )

    run_availability_guard()
    return True


def main() -> int:
    try:
        require_root()
        require_swarm_manager()
        run_availability_guard()

        changed: list[str] = []

        for service_name in APPLICATION_SERVICES:
            if reconcile_service(service_name):
                changed.append(service_name)

        run_availability_guard()

        if changed:
            print(
                "Production runtime policy reconciled for: "
                + ", ".join(changed)
            )
        else:
            print("Production runtime policy already fully reconciled")

        return 0
    except (
        OSError,
        ProductionRuntimePolicyError,
        subprocess.CalledProcessError,
    ) as exception:
        print(f"Production runtime policy reconciliation failed: {exception}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
