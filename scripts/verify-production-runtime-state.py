#!/usr/bin/env python3

from __future__ import annotations

import copy
import sys

from production_deployment_state import build_state
from production_runtime_state import ProductionRuntimeStateError
from production_runtime_state import deployable_catalog
from production_runtime_state import manifest_from_runtime_images
from production_runtime_state import state_drift


BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
TOOLING_SHA = "3" * 40
REGISTRY = "ghcr.io/example"
SWARM_DIGEST = "sha256:" + "a" * 64


def fail(message: str) -> None:
    raise AssertionError(message)


def runtime_images(
    account_sha: str = ACCOUNT_SHA,
) -> dict[str, str]:
    images: dict[str, str] = {}

    for service_name, specification in deployable_catalog().items():
        source_sha = account_sha if service_name == "account-service" else BASE_SHA
        image = f"{REGISTRY}/{specification['image']}:{source_sha}"
        deploy = specification["deploy"]

        if deploy["type"] == "swarm":
            image += f"@{SWARM_DIGEST}"

        images[service_name] = image

    return images


def verify_observed_manifest() -> dict[str, object]:
    manifest = manifest_from_runtime_images(runtime_images())
    services = manifest["services"]

    account = services["account-service"]
    wallet = services["wallet-service"]
    keycloak = services["keycloak"]

    if account["source_sha"] != ACCOUNT_SHA:
        fail("Observed account-service SHA was not preserved")

    if wallet["source_sha"] != BASE_SHA:
        fail("Unchanged wallet-service SHA was not preserved")

    if keycloak["source_sha"] != BASE_SHA:
        fail("Observed Keycloak SHA was not preserved")

    if "@sha256:" in account["image"]:
        fail("Swarm content digest leaked into the release manifest image tag")

    return manifest


def verify_runtime_drift_detection(manifest: dict[str, object]) -> None:
    recorded = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=manifest,
    )
    observed_manifest = manifest_from_runtime_images(
        runtime_images(account_sha=BASE_SHA)
    )
    observed = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=observed_manifest,
    )
    drift = state_drift(
        recorded_state=recorded,
        observed_state=observed,
    )

    if len(drift) != 1 or drift[0]["service"] != "account-service":
        fail(f"Runtime drift did not isolate account-service: {drift}")


def expect_invalid_runtime_images(
    images: dict[str, str],
    description: str,
) -> None:
    try:
        manifest_from_runtime_images(images)
    except ProductionRuntimeStateError:
        return

    fail(f"Invalid runtime image set was accepted: {description}")


def verify_runtime_image_validation() -> None:
    wrong_image = runtime_images()
    wrong_image["account-service"] = (
        f"{REGISTRY}/mnemos-wallet-service:{ACCOUNT_SHA}@{SWARM_DIGEST}"
    )
    expect_invalid_runtime_images(
        images=wrong_image,
        description="wrong catalog image",
    )

    mutable_tag = runtime_images()
    mutable_tag["account-service"] = (
        f"{REGISTRY}/mnemos-account-service:latest@{SWARM_DIGEST}"
    )
    expect_invalid_runtime_images(
        images=mutable_tag,
        description="mutable tag",
    )

    missing_service = copy.deepcopy(runtime_images())
    missing_service.pop("wallet-service")
    expect_invalid_runtime_images(
        images=missing_service,
        description="missing deployable service",
    )


def main() -> int:
    manifest = verify_observed_manifest()
    verify_runtime_drift_detection(manifest)
    verify_runtime_image_validation()

    print("Production runtime state observation behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production runtime state verification failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
