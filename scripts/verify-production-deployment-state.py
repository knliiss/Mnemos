#!/usr/bin/env python3

from __future__ import annotations

import copy
import sys

from production_deployment_plan import build_plan
from production_deployment_state import ProductionDeploymentStateError
from production_deployment_state import build_state
from production_deployment_state import build_transition
from production_deployment_state import validate_state
from production_deployment_state import validate_transition
from production_release_manifest import create_manifest
from production_release_manifest import update_manifest


BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
TOOLING_SHA = "3" * 40
NEXT_TOOLING_SHA = "4" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def verify_service_versions_are_independent_from_tooling() -> None:
    current_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target_manifest = update_manifest(
        base=current_manifest,
        assignments=[("account-service", ACCOUNT_SHA)],
    )

    current_state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=current_manifest,
    )
    target_state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=target_manifest,
    )

    validate_state(current_state)
    validate_state(target_state)

    if target_state["tooling_revision"] != TOOLING_SHA:
        fail("Service hotfix unexpectedly changed the tooling revision")

    account_entry = target_state["release_manifest"]["services"]["account-service"]
    wallet_entry = target_state["release_manifest"]["services"]["wallet-service"]

    if account_entry["source_sha"] != ACCOUNT_SHA:
        fail("Account service hotfix SHA was not retained in deployment state")

    if wallet_entry["source_sha"] != BASE_SHA:
        fail("Unchanged wallet service was moved to the account hotfix SHA")

    if current_state["state_id"] == target_state["state_id"]:
        fail("Different service manifests produced the same deployment state ID")


def verify_tooling_revision_is_independent_from_manifest() -> None:
    manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    current_state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=manifest,
    )
    target_state = build_state(
        tooling_revision=NEXT_TOOLING_SHA,
        release_manifest=manifest,
    )

    if current_state["release_manifest"]["manifest_id"] != target_state["release_manifest"]["manifest_id"]:
        fail("Changing tooling revision unexpectedly changed the service manifest")

    if current_state["state_id"] == target_state["state_id"]:
        fail("Changing tooling revision did not change deployment state identity")


def verify_transition_record() -> None:
    current_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target_manifest = update_manifest(
        base=current_manifest,
        assignments=[("account-service", ACCOUNT_SHA)],
    )
    current_state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=current_manifest,
    )
    target_state = build_state(
        tooling_revision=TOOLING_SHA,
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
        actor="github-actions",
        reason="account-service hotfix",
    )

    validate_transition(
        transition=transition,
        current=current_state,
        target=target_state,
    )

    if transition["plan"]["action_count"] != 1:
        fail("Account hotfix transition does not contain exactly one runtime action")

    if transition["from_tooling_revision"] != transition["to_tooling_revision"]:
        fail("Account hotfix transition unexpectedly changed deployment tooling")

    tampered = copy.deepcopy(transition)
    tampered["reason"] = "different reason"

    try:
        validate_transition(
            transition=tampered,
            current=current_state,
            target=target_state,
        )
    except ProductionDeploymentStateError:
        pass
    else:
        fail("Tampered transition record was accepted")


def verify_state_tamper_detection() -> None:
    manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=manifest,
    )
    tampered = copy.deepcopy(state)
    tampered["tooling_revision"] = NEXT_TOOLING_SHA

    try:
        validate_state(tampered)
    except ProductionDeploymentStateError:
        return

    fail("Tampered deployment state was accepted")


def main() -> int:
    verify_service_versions_are_independent_from_tooling()
    verify_tooling_revision_is_independent_from_manifest()
    verify_transition_record()
    verify_state_tamper_detection()

    print("Production deployment state behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production deployment state verification failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
