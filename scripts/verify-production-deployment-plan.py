#!/usr/bin/env python3

from __future__ import annotations

import copy
import sys

from production_deployment_plan import ProductionDeploymentPlanError
from production_deployment_plan import build_plan
from production_deployment_plan import validate_plan
from production_release_manifest import create_manifest
from production_release_manifest import update_manifest


BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
KEYCLOAK_SHA = "3" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def verify_account_only_plan() -> None:
    current = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target = update_manifest(
        base=current,
        assignments=[("account-service", ACCOUNT_SHA)],
    )
    plan = build_plan(
        current=current,
        target=target,
    )
    validate_plan(plan)

    if plan["mode"] != "scoped":
        fail(f"Account hotfix plan is not scoped: {plan}")

    if plan["action_count"] != 1:
        fail(f"Account hotfix plan should contain one action: {plan}")

    action = plan["actions"][0]

    if action["service"] != "account-service":
        fail(f"Unexpected service in account hotfix plan: {action}")

    if action["operation"] != "image-update":
        fail(f"Account hotfix should be an image update: {action}")

    if action["runtime"] != {
        "type": "swarm",
        "name": "mnemos_account-service",
    }:
        fail(f"Unexpected account runtime target: {action}")

    if action["rollback"]["image"] != action["from"]["image"]:
        fail(f"Account rollback does not restore current image: {action}")


def verify_mixed_plan_order() -> None:
    current = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target = update_manifest(
        base=current,
        assignments=[
            ("account-service", ACCOUNT_SHA),
            ("keycloak", KEYCLOAK_SHA),
        ],
    )
    plan = build_plan(
        current=current,
        target=target,
    )

    if [action["service"] for action in plan["actions"]] != [
        "keycloak",
        "account-service",
    ]:
        fail(f"Compose infrastructure should be planned before Swarm apps: {plan}")


def verify_noop_plan() -> None:
    manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    plan = build_plan(
        current=manifest,
        target=manifest,
    )

    if plan["action_count"] != 0 or plan["actions"]:
        fail(f"Equal manifests must produce an empty scoped plan: {plan}")


def verify_full_reconcile_guard() -> None:
    current = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target = update_manifest(
        base=current,
        assignments=[("account-service", ACCOUNT_SHA)],
    )

    try:
        build_plan(
            current=current,
            target=target,
            requires_full_runtime_reconcile=True,
        )
    except ProductionDeploymentPlanError:
        pass
    else:
        fail("Full runtime reconcile was accepted without explicit permission")

    allowed = build_plan(
        current=current,
        target=target,
        requires_full_runtime_reconcile=True,
        allow_full_runtime_reconcile=True,
    )
    validate_plan(allowed)

    if allowed["mode"] != "full-runtime-reconcile":
        fail(f"Explicit full reconcile is not marked in the plan: {allowed}")

    expected_services = set(target["services"])
    planned_services = {action["service"] for action in allowed["actions"]}

    if planned_services != expected_services:
        fail(
            "Full runtime reconcile must include every deployable service: "
            f"expected={sorted(expected_services)}, actual={sorted(planned_services)}"
        )

    if not all(
        action["operation"] == "runtime-reconcile"
        for action in allowed["actions"]
    ):
        fail(f"Full reconcile contains a scoped image action: {allowed}")

    keycloak_action = allowed["actions"][0]

    if keycloak_action["service"] != "keycloak":
        fail(f"Keycloak should be reconciled before Swarm services: {allowed}")

    account_action = next(
        action
        for action in allowed["actions"]
        if action["service"] == "account-service"
    )

    if account_action["to"]["source_sha"] != ACCOUNT_SHA:
        fail(f"Full reconcile lost the target account image: {account_action}")


def verify_full_reconcile_without_image_change() -> None:
    manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    plan = build_plan(
        current=manifest,
        target=manifest,
        requires_full_runtime_reconcile=True,
        allow_full_runtime_reconcile=True,
    )

    if plan["action_count"] != len(manifest["services"]):
        fail(f"Runtime-only reconcile produced an incomplete plan: {plan}")

    if not all(
        action["from"]["image"] == action["to"]["image"]
        for action in plan["actions"]
    ):
        fail(f"Runtime-only reconcile unexpectedly changed images: {plan}")


def verify_tamper_detection() -> None:
    current = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target = update_manifest(
        base=current,
        assignments=[("account-service", ACCOUNT_SHA)],
    )
    plan = build_plan(
        current=current,
        target=target,
    )
    tampered = copy.deepcopy(plan)
    tampered["actions"][0]["rollback"]["image"] = "ghcr.io/example/wrong:rollback"

    try:
        validate_plan(tampered)
    except ProductionDeploymentPlanError:
        return

    fail("Tampered deployment plan was accepted")


def main() -> int:
    verify_account_only_plan()
    verify_mixed_plan_order()
    verify_noop_plan()
    verify_full_reconcile_guard()
    verify_full_reconcile_without_image_change()
    verify_tamper_detection()

    print("Production deployment plan behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(f"Production deployment plan verification failed: {exception}", file=sys.stderr)
        raise SystemExit(1)
