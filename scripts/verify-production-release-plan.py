#!/usr/bin/env python3

from __future__ import annotations

import tempfile
from pathlib import Path

from production_deployment_plan import ProductionDeploymentPlanError
from production_deployment_state import build_state
from production_release_manifest import create_manifest
from production_release_plan import ProductionReleasePlanError
from production_release_plan import create_release_bundle
from production_release_plan import write_bundle
from production_service_catalog import services as catalog_services


CURRENT_SHA = "1" * 40
TARGET_SHA_A = "2" * 40
TARGET_SHA_B = "3" * 40
TOOLING_SHA = "4" * 40


def fail(message: str) -> None:
    raise AssertionError(message)


def deployable_service_names() -> list[str]:
    return sorted(
        name
        for name, specification in catalog_services().items()
        if isinstance(specification.get("deploy"), dict)
    )


def verify_scoped_release() -> None:
    services = deployable_service_names()

    if len(services) < 2:
        fail("Release planner verification requires at least two deployable services")

    current_manifest = create_manifest(
        registry="example.invalid/mnemos",
        source_sha=CURRENT_SHA,
    )
    current_state = build_state(
        tooling_revision=CURRENT_SHA,
        release_manifest=current_manifest,
    )
    selected = services[:2]

    bundle = create_release_bundle(
        current_state=current_state,
        assignments=[
            (selected[0], TARGET_SHA_A),
            (selected[1], TARGET_SHA_B),
        ],
        tooling_revision=TOOLING_SHA,
        actor="release-verifier",
        reason="verify scoped multi-service release",
    )
    plan = bundle["plan"]

    if not isinstance(plan, dict):
        fail("Release bundle plan is not an object")

    if plan["mode"] != "scoped":
        fail("Multi-service release must remain scoped when runtime reconcile is not required")

    if plan["action_count"] != 2:
        fail("Two service overrides must produce exactly two deployment actions")

    action_services = {action["service"] for action in plan["actions"]}

    if action_services != set(selected):
        fail("Scoped release contains services that were not explicitly overridden")

    for action in plan["actions"]:
        if action["rollback"]["image"] != action["from"]["image"]:
            fail(f"Rollback image is not pinned to prior image for {action['service']}")

    with tempfile.TemporaryDirectory() as directory:
        output_dir = Path(directory)
        write_bundle(bundle=bundle, output_dir=output_dir)

        expected_files = {
            "current-state.json",
            "current-manifest.json",
            "target-manifest.json",
            "target-state.json",
            "deployment-plan.json",
            "transition.json",
        }
        actual_files = {path.name for path in output_dir.iterdir()}

        if actual_files != expected_files:
            fail("Release audit bundle does not contain the complete state/plan transition set")


def verify_empty_scoped_release_is_rejected() -> None:
    current_manifest = create_manifest(
        registry="example.invalid/mnemos",
        source_sha=CURRENT_SHA,
    )
    current_state = build_state(
        tooling_revision=CURRENT_SHA,
        release_manifest=current_manifest,
    )

    try:
        create_release_bundle(
            current_state=current_state,
            assignments=[],
            tooling_revision=CURRENT_SHA,
            actor="release-verifier",
            reason="verify empty release rejection",
        )
    except ProductionReleasePlanError:
        return

    fail("Empty scoped release plan must be rejected")


def verify_full_reconcile_requires_explicit_allow() -> None:
    current_manifest = create_manifest(
        registry="example.invalid/mnemos",
        source_sha=CURRENT_SHA,
    )
    current_state = build_state(
        tooling_revision=CURRENT_SHA,
        release_manifest=current_manifest,
    )

    try:
        create_release_bundle(
            current_state=current_state,
            assignments=[],
            tooling_revision=TOOLING_SHA,
            actor="release-verifier",
            reason="verify full reconcile refusal",
            requires_full_runtime_reconcile=True,
            allow_full_runtime_reconcile=False,
        )
    except ProductionDeploymentPlanError:
        pass
    else:
        fail("Full runtime reconcile must require an explicit allow flag")

    bundle = create_release_bundle(
        current_state=current_state,
        assignments=[],
        tooling_revision=TOOLING_SHA,
        actor="release-verifier",
        reason="verify explicitly allowed full reconcile",
        requires_full_runtime_reconcile=True,
        allow_full_runtime_reconcile=True,
    )
    plan = bundle["plan"]

    if not isinstance(plan, dict):
        fail("Full reconcile plan is not an object")

    if plan["mode"] != "full-runtime-reconcile":
        fail("Explicit full reconcile must produce full-runtime-reconcile mode")

    if plan["action_count"] != len(deployable_service_names()):
        fail("Full reconcile must contain one explicit action per deployable runtime target")


def main() -> int:
    verify_scoped_release()
    verify_empty_scoped_release_is_rejected()
    verify_full_reconcile_requires_explicit_allow()

    print("Production release planning verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
