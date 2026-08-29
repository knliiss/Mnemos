#!/usr/bin/env python3

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path
from typing import Any

from production_deployment_plan import ProductionDeploymentPlanError
from production_deployment_plan import read_plan
from production_deployment_plan import validate_plan
from production_release_manifest import ProductionReleaseManifestError
from production_release_manifest import read_manifest
from production_release_manifest import validate_manifest
from production_release_manifest import validate_source_sha


STATE_SCHEMA_VERSION = 1
TRANSITION_SCHEMA_VERSION = 1


class ProductionDeploymentStateError(RuntimeError):
    pass


def digest(value: dict[str, Any]) -> str:
    canonical = json.dumps(
        value,
        separators=(",", ":"),
        sort_keys=True,
    ).encode("utf-8")

    return "sha256:" + hashlib.sha256(canonical).hexdigest()


def state_identity(
    tooling_revision: str,
    release_manifest: dict[str, Any],
) -> dict[str, Any]:
    return {
        "tooling_revision": tooling_revision,
        "release_manifest_id": release_manifest["manifest_id"],
    }


def build_state(
    tooling_revision: str,
    release_manifest: dict[str, Any],
) -> dict[str, Any]:
    validate_source_sha(tooling_revision)
    validate_manifest(release_manifest)

    identity = state_identity(
        tooling_revision=tooling_revision,
        release_manifest=release_manifest,
    )

    return {
        "schema_version": STATE_SCHEMA_VERSION,
        "state_id": digest(identity),
        "tooling_revision": tooling_revision,
        "release_manifest": release_manifest,
    }


def validate_state(state: dict[str, Any]) -> None:
    if not isinstance(state, dict):
        raise ProductionDeploymentStateError("Deployment state must be an object")

    if state.get("schema_version") != STATE_SCHEMA_VERSION:
        raise ProductionDeploymentStateError("Unsupported deployment state schema")

    tooling_revision = state.get("tooling_revision")
    release_manifest = state.get("release_manifest")

    if not isinstance(tooling_revision, str):
        raise ProductionDeploymentStateError(
            "Deployment state tooling_revision must be a string"
        )

    validate_source_sha(tooling_revision)

    if not isinstance(release_manifest, dict):
        raise ProductionDeploymentStateError(
            "Deployment state release_manifest must be an object"
        )

    validate_manifest(release_manifest)

    expected_state_id = digest(
        state_identity(
            tooling_revision=tooling_revision,
            release_manifest=release_manifest,
        )
    )

    if state.get("state_id") != expected_state_id:
        raise ProductionDeploymentStateError(
            "Deployment state ID does not match tooling revision and release manifest"
        )


def read_state(path: Path) -> dict[str, Any]:
    try:
        state = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ProductionDeploymentStateError(
            f"Deployment state does not exist: {path}"
        ) from exception
    except json.JSONDecodeError as exception:
        raise ProductionDeploymentStateError(
            f"Deployment state is invalid JSON: {exception}"
        ) from exception

    validate_state(state)
    return state


def transition_identity(
    current: dict[str, Any],
    target: dict[str, Any],
    plan: dict[str, Any],
    actor: str,
    reason: str,
) -> dict[str, Any]:
    return {
        "from_state_id": current["state_id"],
        "to_state_id": target["state_id"],
        "plan": plan,
        "actor": actor,
        "reason": reason,
    }


def build_transition(
    current: dict[str, Any],
    target: dict[str, Any],
    plan: dict[str, Any],
    actor: str,
    reason: str,
) -> dict[str, Any]:
    validate_state(current)
    validate_state(target)
    validate_plan(plan)

    actor = actor.strip()
    reason = reason.strip()

    if not actor:
        raise ProductionDeploymentStateError("Transition actor must not be blank")

    if not reason:
        raise ProductionDeploymentStateError("Transition reason must not be blank")

    current_manifest_id = current["release_manifest"]["manifest_id"]
    target_manifest_id = target["release_manifest"]["manifest_id"]

    if plan["current_manifest_id"] != current_manifest_id:
        raise ProductionDeploymentStateError(
            "Deployment plan current manifest does not match current deployment state"
        )

    if plan["target_manifest_id"] != target_manifest_id:
        raise ProductionDeploymentStateError(
            "Deployment plan target manifest does not match target deployment state"
        )

    identity = transition_identity(
        current=current,
        target=target,
        plan=plan,
        actor=actor,
        reason=reason,
    )

    return {
        "schema_version": TRANSITION_SCHEMA_VERSION,
        "transition_id": digest(identity),
        "from_state_id": current["state_id"],
        "to_state_id": target["state_id"],
        "from_tooling_revision": current["tooling_revision"],
        "to_tooling_revision": target["tooling_revision"],
        "from_manifest_id": current_manifest_id,
        "to_manifest_id": target_manifest_id,
        "actor": actor,
        "reason": reason,
        "plan": plan,
    }


def validate_transition(
    transition: dict[str, Any],
    current: dict[str, Any],
    target: dict[str, Any],
) -> None:
    if not isinstance(transition, dict):
        raise ProductionDeploymentStateError("Deployment transition must be an object")

    if transition.get("schema_version") != TRANSITION_SCHEMA_VERSION:
        raise ProductionDeploymentStateError(
            "Unsupported deployment transition schema"
        )

    validate_state(current)
    validate_state(target)

    plan = transition.get("plan")
    actor = transition.get("actor")
    reason = transition.get("reason")

    if not isinstance(plan, dict):
        raise ProductionDeploymentStateError("Deployment transition plan is invalid")

    validate_plan(plan)

    if not isinstance(actor, str) or not actor.strip():
        raise ProductionDeploymentStateError("Deployment transition actor is invalid")

    if not isinstance(reason, str) or not reason.strip():
        raise ProductionDeploymentStateError("Deployment transition reason is invalid")

    expected = build_transition(
        current=current,
        target=target,
        plan=plan,
        actor=actor,
        reason=reason,
    )

    for field_name in (
        "transition_id",
        "from_state_id",
        "to_state_id",
        "from_tooling_revision",
        "to_tooling_revision",
        "from_manifest_id",
        "to_manifest_id",
    ):
        if transition.get(field_name) != expected[field_name]:
            raise ProductionDeploymentStateError(
                f"Deployment transition field does not match state: {field_name}"
            )


def read_transition(
    path: Path,
    current: dict[str, Any],
    target: dict[str, Any],
) -> dict[str, Any]:
    try:
        transition = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ProductionDeploymentStateError(
            f"Deployment transition does not exist: {path}"
        ) from exception
    except json.JSONDecodeError as exception:
        raise ProductionDeploymentStateError(
            f"Deployment transition is invalid JSON: {exception}"
        ) from exception

    validate_transition(
        transition=transition,
        current=current,
        target=target,
    )
    return transition


def write_json(value: dict[str, Any], output: Path | None) -> None:
    serialized = json.dumps(value, indent=2, sort_keys=False) + "\n"

    if output is None:
        sys.stdout.write(serialized)
    else:
        output.write_text(serialized, encoding="utf-8")


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Create and validate Mnemos production deployment state and transition "
            "records. This tool never changes the production runtime."
        ),
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    state_create = subparsers.add_parser("state-create")
    state_create.add_argument("--tooling-revision", required=True)
    state_create.add_argument("--manifest", type=Path, required=True)
    state_create.add_argument("--output", type=Path)

    state_validate = subparsers.add_parser("state-validate")
    state_validate.add_argument("--input", type=Path, required=True)

    transition_create = subparsers.add_parser("transition-create")
    transition_create.add_argument("--current-state", type=Path, required=True)
    transition_create.add_argument("--target-state", type=Path, required=True)
    transition_create.add_argument("--plan", type=Path, required=True)
    transition_create.add_argument("--actor", required=True)
    transition_create.add_argument("--reason", required=True)
    transition_create.add_argument("--output", type=Path)

    transition_validate = subparsers.add_parser("transition-validate")
    transition_validate.add_argument("--input", type=Path, required=True)
    transition_validate.add_argument("--current-state", type=Path, required=True)
    transition_validate.add_argument("--target-state", type=Path, required=True)

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "state-create":
            manifest = read_manifest(arguments.manifest)
            state = build_state(
                tooling_revision=arguments.tooling_revision,
                release_manifest=manifest,
            )
            write_json(state, arguments.output)
            return 0

        if arguments.command == "state-validate":
            read_state(arguments.input)
            print("Production deployment state verified")
            return 0

        if arguments.command == "transition-create":
            current = read_state(arguments.current_state)
            target = read_state(arguments.target_state)
            plan = read_plan(arguments.plan)
            transition = build_transition(
                current=current,
                target=target,
                plan=plan,
                actor=arguments.actor,
                reason=arguments.reason,
            )
            write_json(transition, arguments.output)
            return 0

        if arguments.command == "transition-validate":
            current = read_state(arguments.current_state)
            target = read_state(arguments.target_state)
            read_transition(
                path=arguments.input,
                current=current,
                target=target,
            )
            print("Production deployment transition verified")
            return 0
    except (
        OSError,
        ProductionDeploymentPlanError,
        ProductionDeploymentStateError,
        ProductionReleaseManifestError,
    ) as exception:
        print(f"Production deployment state failed: {exception}", file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
