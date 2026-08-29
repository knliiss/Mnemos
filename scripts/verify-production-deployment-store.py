#!/usr/bin/env python3

from __future__ import annotations

import os
import sys
import tempfile
from pathlib import Path

from production_deployment_state import build_state
from production_deployment_store import ProductionDeploymentStoreError
from production_deployment_store import initialize_current_state
from production_deployment_store import read_current_state
from production_deployment_store import replace_current_state
from production_deployment_store import state_snapshot_path
from production_release_manifest import create_manifest
from production_release_manifest import update_manifest


BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
TOOLING_SHA = "3" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def states() -> tuple[dict[str, object], dict[str, object]]:
    current_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target_manifest = update_manifest(
        base=current_manifest,
        assignments=[("account-service", ACCOUNT_SHA)],
    )

    return (
        build_state(
            tooling_revision=TOOLING_SHA,
            release_manifest=current_manifest,
        ),
        build_state(
            tooling_revision=TOOLING_SHA,
            release_manifest=target_manifest,
        ),
    )


def verify_initialize_and_replace() -> None:
    initial_state, target_state = states()

    with tempfile.TemporaryDirectory() as directory:
        state_dir = Path(directory) / "deploy"
        result = initialize_current_state(
            state_dir=state_dir,
            state=initial_state,
            expected_state_id=initial_state["state_id"],
        )

        if result != "initialized":
            fail(f"Unexpected deployment state initialization result: {result}")

        if read_current_state(state_dir) != initial_state:
            fail("Initialized current deployment state does not match input state")

        snapshot = state_snapshot_path(
            state_dir=state_dir,
            state_id=initial_state["state_id"],
        )

        if not snapshot.is_file():
            fail("Initial deployment state snapshot was not persisted")

        if (state_dir.stat().st_mode & 0o777) != 0o700:
            fail("Deployment state directory is not private")

        if (snapshot.stat().st_mode & 0o777) != 0o600:
            fail("Deployment state snapshot is not private")

        repeated = initialize_current_state(
            state_dir=state_dir,
            state=initial_state,
            expected_state_id=initial_state["state_id"],
        )

        if repeated != "already-initialized":
            fail(f"Idempotent state initialization failed: {repeated}")

        try:
            initialize_current_state(
                state_dir=state_dir,
                state=target_state,
                expected_state_id=target_state["state_id"],
            )
        except ProductionDeploymentStoreError:
            pass
        else:
            fail("State initialization overwrote an existing different current state")

        replace_current_state(
            state_dir=state_dir,
            state=target_state,
            expected_state_id=target_state["state_id"],
        )

        if read_current_state(state_dir) != target_state:
            fail("Explicit deployment state replacement did not update current state")

        target_snapshot = state_snapshot_path(
            state_dir=state_dir,
            state_id=target_state["state_id"],
        )

        if not target_snapshot.is_file():
            fail("Replacement deployment state snapshot was not persisted")

        if not snapshot.is_file():
            fail("Replacing current state removed the previous immutable snapshot")


def verify_expected_state_guard() -> None:
    initial_state, _ = states()

    with tempfile.TemporaryDirectory() as directory:
        state_dir = Path(directory) / "deploy"

        try:
            initialize_current_state(
                state_dir=state_dir,
                state=initial_state,
                expected_state_id="sha256:" + "0" * 64,
            )
        except ProductionDeploymentStoreError:
            if state_dir.exists() and any(state_dir.iterdir()):
                fail("Rejected state initialization still mutated the state directory")
            return

    fail("Deployment state initialization accepted the wrong expected state ID")


def main() -> int:
    verify_initialize_and_replace()
    verify_expected_state_guard()

    print("Production deployment state store behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production deployment state store verification failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
