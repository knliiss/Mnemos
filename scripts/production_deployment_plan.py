#!/usr/bin/env python3

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path
from typing import Any

from production_release_manifest import ProductionReleaseManifestError
from production_release_manifest import manifest_diff
from production_release_manifest import read_manifest


PLAN_SCHEMA_VERSION = 1
DEFAULT_STABLE_SECONDS = 15


class ProductionDeploymentPlanError(RuntimeError):
    pass


def verification_policy(deploy_type: str) -> dict[str, Any]:
    if deploy_type == "swarm":
        return {
            "strategy": "service-convergence",
            "require_desired_replicas": True,
            "require_update_completed": True,
            "require_healthy_containers": True,
            "stable_seconds": DEFAULT_STABLE_SECONDS,
        }

    if deploy_type == "compose":
        return {
            "strategy": "container-health",
            "require_running": True,
            "require_healthy": True,
            "stable_seconds": DEFAULT_STABLE_SECONDS,
        }

    raise ProductionDeploymentPlanError(
        f"Unsupported production deployment type: {deploy_type}"
    )


def action_for_change(change: dict[str, Any]) -> dict[str, Any]:
    deploy = change.get("deploy")

    if not isinstance(deploy, dict):
        raise ProductionDeploymentPlanError(
            f"{change.get('service', '<unknown>')} does not define deployment metadata"
        )

    deploy_type = deploy.get("type")
    runtime_name = deploy.get("name")

    if not isinstance(deploy_type, str) or not deploy_type:
        raise ProductionDeploymentPlanError("Deployment type must be a non-empty string")

    if not isinstance(runtime_name, str) or not runtime_name:
        raise ProductionDeploymentPlanError("Runtime name must be a non-empty string")

    return {
        "service": change["service"],
        "runtime": {
            "type": deploy_type,
            "name": runtime_name,
        },
        "operation": "image-update",
        "from": {
            "image": change["from_image"],
            "source_sha": change["from_source_sha"],
        },
        "to": {
            "image": change["to_image"],
            "source_sha": change["to_source_sha"],
        },
        "verification": verification_policy(deploy_type),
        "rollback": {
            "operation": "image-update",
            "image": change["from_image"],
            "source_sha": change["from_source_sha"],
        },
    }


def action_order(action: dict[str, Any]) -> tuple[int, str]:
    runtime_type = action["runtime"]["type"]

    if runtime_type == "compose":
        priority = 0
    elif runtime_type == "swarm":
        priority = 1
    else:
        priority = 100

    return priority, action["service"]


def plan_id(
    current_manifest_id: str,
    target_manifest_id: str,
    actions: list[dict[str, Any]],
) -> str:
    canonical = json.dumps(
        {
            "current_manifest_id": current_manifest_id,
            "target_manifest_id": target_manifest_id,
            "actions": actions,
        },
        separators=(",", ":"),
        sort_keys=True,
    ).encode("utf-8")

    return "sha256:" + hashlib.sha256(canonical).hexdigest()


def build_plan(
    current: dict[str, Any],
    target: dict[str, Any],
    requires_full_runtime_reconcile: bool = False,
    allow_full_runtime_reconcile: bool = False,
) -> dict[str, Any]:
    if requires_full_runtime_reconcile and not allow_full_runtime_reconcile:
        raise ProductionDeploymentPlanError(
            "Changed deployment configuration requires a full runtime reconcile. "
            "Refusing to produce a scoped plan without explicit --allow-full-runtime-reconcile."
        )

    changes = manifest_diff(
        current=current,
        target=target,
    )
    actions = sorted(
        (action_for_change(change) for change in changes),
        key=action_order,
    )

    current_manifest_id = current["manifest_id"]
    target_manifest_id = target["manifest_id"]

    return {
        "schema_version": PLAN_SCHEMA_VERSION,
        "plan_id": plan_id(
            current_manifest_id=current_manifest_id,
            target_manifest_id=target_manifest_id,
            actions=actions,
        ),
        "current_manifest_id": current_manifest_id,
        "target_manifest_id": target_manifest_id,
        "mode": (
            "full-runtime-reconcile"
            if requires_full_runtime_reconcile
            else "scoped"
        ),
        "action_count": len(actions),
        "actions": actions,
    }


def validate_plan(plan: dict[str, Any]) -> None:
    if not isinstance(plan, dict):
        raise ProductionDeploymentPlanError("Deployment plan must be an object")

    if plan.get("schema_version") != PLAN_SCHEMA_VERSION:
        raise ProductionDeploymentPlanError("Unsupported deployment plan schema")

    actions = plan.get("actions")

    if not isinstance(actions, list):
        raise ProductionDeploymentPlanError("Deployment plan actions must be a list")

    if plan.get("action_count") != len(actions):
        raise ProductionDeploymentPlanError("Deployment action count is inconsistent")

    services: set[str] = set()

    for action in actions:
        if not isinstance(action, dict):
            raise ProductionDeploymentPlanError("Deployment action must be an object")

        service = action.get("service")

        if not isinstance(service, str) or not service:
            raise ProductionDeploymentPlanError("Deployment action service is invalid")

        if service in services:
            raise ProductionDeploymentPlanError(
                f"Deployment plan contains duplicate service action: {service}"
            )

        services.add(service)

        if action.get("operation") != "image-update":
            raise ProductionDeploymentPlanError(
                f"Unsupported deployment operation for {service}"
            )

        runtime = action.get("runtime")
        source = action.get("from")
        target = action.get("to")
        rollback = action.get("rollback")
        verification = action.get("verification")

        if not all(
            isinstance(value, dict)
            for value in (runtime, source, target, rollback, verification)
        ):
            raise ProductionDeploymentPlanError(
                f"{service} deployment action is incomplete"
            )

        if rollback.get("image") != source.get("image"):
            raise ProductionDeploymentPlanError(
                f"{service} rollback image does not match current image"
            )

        if rollback.get("source_sha") != source.get("source_sha"):
            raise ProductionDeploymentPlanError(
                f"{service} rollback source SHA does not match current source SHA"
            )

        verification_policy(str(runtime.get("type")))

    expected_plan_id = plan_id(
        current_manifest_id=str(plan.get("current_manifest_id")),
        target_manifest_id=str(plan.get("target_manifest_id")),
        actions=actions,
    )

    if plan.get("plan_id") != expected_plan_id:
        raise ProductionDeploymentPlanError(
            "Deployment plan ID does not match plan contents"
        )


def write_plan(plan: dict[str, Any], output: Path | None) -> None:
    serialized = json.dumps(plan, indent=2, sort_keys=False) + "\n"

    if output is None:
        sys.stdout.write(serialized)
    else:
        output.write_text(serialized, encoding="utf-8")


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Create or validate a deterministic Mnemos production deployment plan. "
            "This tool never executes Docker or changes production."
        ),
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    create_parser = subparsers.add_parser("create")
    create_parser.add_argument("--current", type=Path, required=True)
    create_parser.add_argument("--target", type=Path, required=True)
    create_parser.add_argument("--output", type=Path)
    create_parser.add_argument(
        "--requires-full-runtime-reconcile",
        action="store_true",
    )
    create_parser.add_argument(
        "--allow-full-runtime-reconcile",
        action="store_true",
    )

    validate_parser = subparsers.add_parser("validate")
    validate_parser.add_argument("--input", type=Path, required=True)

    return parser.parse_args()


def read_plan(path: Path) -> dict[str, Any]:
    try:
        plan = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ProductionDeploymentPlanError(
            f"Deployment plan does not exist: {path}"
        ) from exception
    except json.JSONDecodeError as exception:
        raise ProductionDeploymentPlanError(
            f"Deployment plan is invalid JSON: {exception}"
        ) from exception

    validate_plan(plan)
    return plan


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "create":
            current = read_manifest(arguments.current)
            target = read_manifest(arguments.target)
            plan = build_plan(
                current=current,
                target=target,
                requires_full_runtime_reconcile=(
                    arguments.requires_full_runtime_reconcile
                ),
                allow_full_runtime_reconcile=(
                    arguments.allow_full_runtime_reconcile
                ),
            )
            validate_plan(plan)
            write_plan(plan, arguments.output)
            return 0

        if arguments.command == "validate":
            read_plan(arguments.input)
            print("Production deployment plan verified")
            return 0
    except (
        OSError,
        ProductionDeploymentPlanError,
        ProductionReleaseManifestError,
    ) as exception:
        print(f"Production deployment plan failed: {exception}", file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
