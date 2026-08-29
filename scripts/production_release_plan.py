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
from production_release_manifest import parse_assignment
from production_release_manifest import update_manifest
from production_release_manifest import validate_manifest
from production_release_manifest import validate_source_sha
from production_source_revision import ProductionSourceRevisionError
from production_source_revision import require_trusted_source_revision


class ProductionReleasePlanError(RuntimeError):
    pass


def create_release_bundle(
    current_state: dict[str, object],
    assignments: list[tuple[str, str]],
    tooling_revision: str,
    actor: str,
    reason: str,
    requires_full_runtime_reconcile: bool = False,
    allow_full_runtime_reconcile: bool = False,
) -> dict[str, object]:
    validate_state(current_state)
    validate_source_sha(tooling_revision)

    current_manifest = current_state.get("release_manifest")

    if not isinstance(current_manifest, dict):
        raise ProductionReleasePlanError(
            "Current deployment state does not contain a release manifest"
        )

    validate_manifest(current_manifest)

    if assignments:
        target_manifest = update_manifest(
            base=current_manifest,
            assignments=assignments,
        )
    else:
        target_manifest = json.loads(json.dumps(current_manifest))

    target_state = build_state(
        tooling_revision=tooling_revision,
        release_manifest=target_manifest,
    )
    plan = build_plan(
        current=current_manifest,
        target=target_manifest,
        requires_full_runtime_reconcile=requires_full_runtime_reconcile,
        allow_full_runtime_reconcile=allow_full_runtime_reconcile,
    )

    if plan["action_count"] == 0:
        raise ProductionReleasePlanError(
            "Release plan contains no runtime actions. Provide at least one service "
            "override or explicitly request a full runtime reconcile."
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

    return {
        "current_state": current_state,
        "current_manifest": current_manifest,
        "target_manifest": target_manifest,
        "target_state": target_state,
        "plan": plan,
        "transition": transition,
    }


def write_bundle(bundle: dict[str, object], output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)

    files = {
        "current-state.json": bundle["current_state"],
        "current-manifest.json": bundle["current_manifest"],
        "target-manifest.json": bundle["target_manifest"],
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
            "Create an auditable multi-service Mnemos production release plan bundle. "
            "This tool only plans state and never executes runtime changes."
        ),
    )
    parser.add_argument("--current-state", type=Path, required=True)
    parser.add_argument(
        "--set",
        dest="assignments",
        action="append",
        type=parse_assignment,
        default=[],
        help="Target service revision in SERVICE=FULL_COMMIT_SHA form. Repeat as needed.",
    )
    parser.add_argument(
        "--tooling-revision",
        help=(
            "Deployment tooling revision for the target state. Defaults to the "
            "current state's tooling revision."
        ),
    )
    parser.add_argument("--requires-full-runtime-reconcile", action="store_true")
    parser.add_argument("--allow-full-runtime-reconcile", action="store_true")
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
            raise ProductionReleasePlanError(
                "Target tooling revision must be a string"
            )

        require_trusted_source_revision(tooling_revision)

        for _, source_sha in arguments.assignments:
            require_trusted_source_revision(source_sha)

        bundle = create_release_bundle(
            current_state=current_state,
            assignments=arguments.assignments,
            tooling_revision=tooling_revision,
            actor=arguments.actor,
            reason=arguments.reason,
            requires_full_runtime_reconcile=arguments.requires_full_runtime_reconcile,
            allow_full_runtime_reconcile=arguments.allow_full_runtime_reconcile,
        )
        write_bundle(
            bundle=bundle,
            output_dir=arguments.output_dir,
        )

        plan = bundle["plan"]

        if not isinstance(plan, dict):
            raise ProductionReleasePlanError("Generated deployment plan is invalid")

        print(
            "Production release plan created: "
            f"{plan['plan_id']} ({plan['action_count']} actions)"
        )
        return 0
    except (
        OSError,
        ProductionDeploymentPlanError,
        ProductionDeploymentStateError,
        ProductionReleaseManifestError,
        ProductionReleasePlanError,
        ProductionSourceRevisionError,
    ) as exception:
        print(f"Production release planning failed: {exception}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
