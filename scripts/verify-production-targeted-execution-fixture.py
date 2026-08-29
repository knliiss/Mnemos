#!/usr/bin/env python3

from __future__ import annotations

import json
import subprocess
import sys
import tempfile
from pathlib import Path

from production_audit_bundle import validate_manifest
from production_audit_bundle import write_manifest
from production_deployment_state import build_state
from production_hotfix_plan import create_hotfix_bundle
from production_hotfix_plan import write_bundle
from production_release_manifest import create_manifest


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
BASE_SHA = "1" * 40
TARGET_SHA = "2" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def verify_fixture_dry_run() -> None:
    current_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    current_state = build_state(
        tooling_revision=BASE_SHA,
        release_manifest=current_manifest,
    )
    bundle = create_hotfix_bundle(
        current_state=current_state,
        service="account-service",
        source_sha=TARGET_SHA,
        tooling_revision=BASE_SHA,
        actor="fixture-verifier",
        reason="fixture-only targeted execution validation",
    )

    with tempfile.TemporaryDirectory() as directory:
        root = Path(directory)
        plan_dir = root / "plan"
        state_dir = root / "state"
        state_dir.mkdir(parents=True)

        write_bundle(
            bundle=bundle,
            output_dir=plan_dir,
        )
        (plan_dir / "current-state.json").write_text(
            json.dumps(current_state, indent=2) + "\n",
            encoding="utf-8",
        )
        (state_dir / "current-state.json").write_text(
            json.dumps(current_state, indent=2) + "\n",
            encoding="utf-8",
        )

        write_manifest(
            directory=plan_dir,
            bundle_kind="hotfix-plan",
        )
        validate_manifest(
            directory=plan_dir,
            expected_kind="hotfix-plan",
        )

        transition = bundle["transition"]

        if not isinstance(transition, dict):
            fail("Fixture transition is invalid")

        command = [
            sys.executable,
            str(REPOSITORY_ROOT / "scripts" / "production_deployment_executor.py"),
            "--transition",
            str(plan_dir / "transition.json"),
            "--target-state",
            str(plan_dir / "target-state.json"),
            "--state-dir",
            str(state_dir),
            "--expected-transition-id",
            str(transition["transition_id"]),
        ]
        completed = subprocess.run(
            command,
            cwd=REPOSITORY_ROOT,
            check=True,
            capture_output=True,
            text=True,
        )
        dry_run = json.loads(completed.stdout)

        if dry_run.get("transition_id") != transition["transition_id"]:
            fail("Executor dry run did not preserve the approved transition ID")

        if dry_run.get("action_count") != 1:
            fail("Fixture hotfix must contain exactly one targeted action")

        actions = dry_run.get("actions")

        if not isinstance(actions, list) or len(actions) != 1:
            fail("Executor dry run has an invalid action list")

        action = actions[0]

        if action.get("service") != "account-service":
            fail("Executor dry run targets the wrong service")

        apply_steps = action.get("apply")

        if not isinstance(apply_steps, list) or not apply_steps:
            fail("Executor dry run does not contain apply commands")

        update_command = apply_steps[0].get("command")

        if not isinstance(update_command, list):
            fail("Executor dry run update command is invalid")

        expected_prefix = [
            "docker",
            "service",
            "update",
            "--detach=true",
            "--with-registry-auth",
            "--image",
            f"{REGISTRY}/mnemos-account-service:{TARGET_SHA}",
            "mnemos_account-service",
        ]

        if update_command != expected_prefix:
            fail(f"Executor dry run is not scoped to account-service: {update_command}")

        if (state_dir / "transitions").exists():
            fail("Fixture dry run unexpectedly persisted an execution attempt")

        persisted_state = json.loads(
            (state_dir / "current-state.json").read_text(encoding="utf-8")
        )

        if persisted_state != current_state:
            fail("Fixture dry run unexpectedly changed deployment state")


def main() -> int:
    verify_fixture_dry_run()
    print("Production targeted execution fixture dry run verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production targeted execution fixture failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
