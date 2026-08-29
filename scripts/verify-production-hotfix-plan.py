#!/usr/bin/env python3

from __future__ import annotations

import sys

from production_deployment_state import build_state
from production_hotfix_plan import ProductionHotfixPlanError
from production_hotfix_plan import create_hotfix_bundle
from production_release_manifest import create_manifest


BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
TOOLING_SHA = "3" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def main() -> int:
    current_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    current_state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=current_manifest,
    )
    bundle = create_hotfix_bundle(
        current_state=current_state,
        service="account-service",
        source_sha=ACCOUNT_SHA,
        tooling_revision=TOOLING_SHA,
        actor="github-actions",
        reason="account-service hotfix",
    )

    plan = bundle["plan"]
    target_state = bundle["target_state"]

    if not isinstance(plan, dict) or plan.get("action_count") != 1:
        fail(f"Account hotfix did not produce exactly one action: {plan}")

    action = plan["actions"][0]

    if action["service"] != "account-service":
        fail(f"Account hotfix targeted the wrong service: {action}")

    if action["runtime"]["name"] != "mnemos_account-service":
        fail(f"Account hotfix targeted the wrong runtime: {action}")

    if not isinstance(target_state, dict):
        fail("Hotfix target state is not an object")

    services = target_state["release_manifest"]["services"]

    if services["account-service"]["source_sha"] != ACCOUNT_SHA:
        fail("Account hotfix target state does not use the requested SHA")

    if services["wallet-service"]["source_sha"] != BASE_SHA:
        fail("Account hotfix unexpectedly changed wallet-service")

    if target_state["tooling_revision"] != TOOLING_SHA:
        fail("Account hotfix unexpectedly changed deployment tooling")

    try:
        create_hotfix_bundle(
            current_state=current_state,
            service="account-service",
            source_sha=BASE_SHA,
            tooling_revision=TOOLING_SHA,
            actor="github-actions",
            reason="no-op hotfix",
        )
    except ProductionHotfixPlanError:
        pass
    else:
        fail("No-op hotfix was accepted as a scoped deployment")

    print("Production scoped hotfix bundle behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production scoped hotfix bundle verification failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
