#!/usr/bin/env python3

from __future__ import annotations

import copy
import sys

from production_release_manifest import ProductionReleaseManifestError
from production_release_manifest import create_manifest
from production_release_manifest import manifest_diff
from production_release_manifest import update_manifest
from production_release_manifest import validate_manifest


BASE_SHA = "1" * 40
ACCOUNT_HOTFIX_SHA = "2" * 40
KEYCLOAK_HOTFIX_SHA = "3" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def verify_initial_manifest() -> dict[str, object]:
    manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    validate_manifest(manifest)

    services = manifest["services"]

    if "account-service" not in services:
        fail("account-service is missing from the release manifest")

    if "keycloak" not in services:
        fail("keycloak is missing from the release manifest")

    return manifest


def verify_single_service_hotfix(base: dict[str, object]) -> dict[str, object]:
    target = update_manifest(
        base=base,
        assignments=[("account-service", ACCOUNT_HOTFIX_SHA)],
    )
    changes = manifest_diff(
        current=base,
        target=target,
    )

    if len(changes) != 1:
        fail(f"Expected one changed service, got {changes}")

    change = changes[0]

    if change["service"] != "account-service":
        fail(f"Unexpected changed service: {change}")

    if change["to_source_sha"] != ACCOUNT_HOTFIX_SHA:
        fail(f"Unexpected account-service target SHA: {change}")

    if target["services"]["keycloak"] != base["services"]["keycloak"]:
        fail("account-service hotfix unexpectedly changed Keycloak")

    return target


def verify_independent_keycloak_hotfix(
    base: dict[str, object],
    account_hotfix: dict[str, object],
) -> None:
    target = update_manifest(
        base=account_hotfix,
        assignments=[("keycloak", KEYCLOAK_HOTFIX_SHA)],
    )
    changes = manifest_diff(
        current=account_hotfix,
        target=target,
    )

    if [change["service"] for change in changes] != ["keycloak"]:
        fail(f"Expected only Keycloak to change, got {changes}")

    if target["services"]["account-service"]["source_sha"] != ACCOUNT_HOTFIX_SHA:
        fail("Keycloak hotfix lost the existing account-service hotfix")

    all_changes = manifest_diff(
        current=base,
        target=target,
    )

    if [change["service"] for change in all_changes] != [
        "account-service",
        "keycloak",
    ]:
        fail(f"Unexpected cumulative release diff: {all_changes}")


def expect_tamper_rejected(
    manifest: dict[str, object],
    description: str,
) -> None:
    try:
        validate_manifest(manifest)
    except ProductionReleaseManifestError:
        return

    fail(f"Tampered release manifest was accepted: {description}")


def verify_tamper_detection(base: dict[str, object]) -> None:
    image_tampered = copy.deepcopy(base)
    image_tampered["services"]["account-service"]["image"] = (
        "ghcr.io/example/wrong:broken"
    )
    expect_tamper_rejected(
        manifest=image_tampered,
        description="image",
    )

    kind_tampered = copy.deepcopy(base)
    kind_tampered["services"]["account-service"]["kind"] = "infrastructure"
    expect_tamper_rejected(
        manifest=kind_tampered,
        description="kind",
    )


def main() -> int:
    base = verify_initial_manifest()
    account_hotfix = verify_single_service_hotfix(base)
    verify_independent_keycloak_hotfix(
        base=base,
        account_hotfix=account_hotfix,
    )
    verify_tamper_detection(base)

    print("Production release manifest behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(f"Production release manifest verification failed: {exception}", file=sys.stderr)
        raise SystemExit(1)
