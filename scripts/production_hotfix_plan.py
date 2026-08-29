#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from production_deployment_plan import ProductionDeploymentPlanError
from production_deployment_plan import build_plan
from production_deployment_plan import validate_plan
from production_deployment_state import ProductionDeploymentStateError
from production_deployment_state import build_state
from production_deployment_state import build_transition
from production_deployment_state import read_state
from production_deployment_state import validate_state
from production_deployment_state import validate_transition
from production_release_manifest import ProductionReleaseManifestError
from production_release_manifest import update_manifest
from production_release_manifest import validate_source_sha


class ProductionHotfixPlanError(RuntimeError):
    pass


def create_hotfix_bundle(
    current_state: dict[str, object],
    service: str,
    source_sha: str,
    tooling_revision: str,
    actor: str,
    reason: str,
) -> dict[str, object]:
    validate_state(current_state)
    validate_source_sha(source_sha)
    validate_source_sha(tooling_revision)

    current_manifest = current_state["release_manifest"]

    if not isinstance(current_manifest, dict):
        raise ProductionHotfixPlanError(
            "Current deployment state does not contain a release manifest"
        )

    target_manifest = update_manifest(
        base=current_manifest,
        assignments=[(service, source_sha)],
    )
    target_state = build_state(
        tooling_revision=tooling_revision,
        release_manifest=target_manifest,
    )
    plan = build_plan(
        current=current_manifest,
        target=target_manifest,
    )
    transition = build_transition(
        current=current_state,
        target=target_state,
        plan=plan,
        actor=actor,
        reason=reason,
    )

    validate_state(target_state)
    validate_plan(plan)
    validate_transition(
        transition=transition,
        current=current_state,
        target=target_state,
    )

    if plan["action_count"] != 1:
        raise ProductionHotfixPlanError(
            "A scoped single-service hotfix must produce exactly one deployment action"
        )

    action = plan["actions"][0]

    if action["service"] != service:
        raise ProductionHotfixPlanError(
            f"Hotfix plan targets {action['service']} instead of requested {service}"
        )

    return {
        "current_state_id": current_state["state_id"],
        "target_state": target_state,
        "plan": plan,
        "transition": transition,
    }


def write_bundle(bundle: dict[str, object], output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)

    files = {
        "target-state.json": bundle["target_state"],
        "deployment-plan.json": bundle["plan"],
        "transition.json": bundle["transition"],
    }

    for file_name, value in files.items():
        path = output_dir / file_name
        path.write_text(
            json.dumps(value, indent=2, sort_keys=False) + "\n",
            encoding="utf-8",
        )


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Create a scoped single-service Mnemos production hotfix bundle. "
            "This tool only plans state and never executes runtime changes."
        ),
    )
    parser.add_argument("--current-state", type=Path, required=True)
    parser.add_argument("--service", required=True)
    parser.add_argument("--source-sha", required=True)
    parser.add_argument(
        "--tooling-revision",
        help=(
            "Deployment tooling revision for the target state. Defaults to the "
            "current state's tooling revision."
        ),
    )
    parser.add_argument("--actor", required=True)
    parser.add_argument("--reason", required=True)
    parser.add_argument("--output-dir", type=Path, required=True)

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        current_state = read_state(arguments.current_state)
        tooling_revision = (
            arguments.tooling_revision
            if arguments.tooling_revision is not None
            else current_state["tooling_revision"]
        )

        if not isinstance(tooling_revision, str):
            raise ProductionHotfixPlanError(
                "Target tooling revision must be a string"
            )

        bundle = create_hotfix_bundle(
            current_state=current_state,
            service=arguments.service,
            source_sha=arguments.source_sha,
            tooling_revision=tooling_revision,
            actor=arguments.actor,
            reason=arguments.reason,
        )
        write_bundle(
            bundle=bundle,
            output_dir=arguments.output_dir,
        )

        print(
            "Production hotfix plan created: "
            f"{bundle['plan']['plan_id']}"
        )
        return 0
    except (
        OSError,
        ProductionDeploymentPlanError,
        ProductionDeploymentStateError,
        ProductionHotfixPlanError,
        ProductionReleaseManifestError,
    ) as exception:
        print(f"Production hotfix planning failed: {exception}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
