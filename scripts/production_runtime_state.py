#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import subprocess
import sys
from pathlib import Path
from typing import Any

from production_deployment_state import ProductionDeploymentStateError
from production_deployment_state import build_state
from production_deployment_state import read_state
from production_release_manifest import ProductionReleaseManifestError
from production_release_manifest import build_manifest
from production_release_manifest import validate_manifest
from production_release_manifest import validate_source_sha
from production_service_catalog import ProductionServiceCatalogError
from production_service_catalog import services as catalog_services


class ProductionRuntimeStateError(RuntimeError):
    pass


def deployable_catalog() -> dict[str, dict[str, Any]]:
    return {
        name: specification
        for name, specification in catalog_services().items()
        if isinstance(specification.get("deploy"), dict)
    }


def inspect_value(command: list[str], description: str) -> str:
    try:
        result = subprocess.run(
            command,
            check=True,
            capture_output=True,
            text=True,
        )
    except subprocess.CalledProcessError as exception:
        stderr = exception.stderr.strip() if exception.stderr else ""
        detail = f": {stderr}" if stderr else ""
        raise ProductionRuntimeStateError(
            f"Could not inspect {description}{detail}"
        ) from exception

    value = result.stdout.strip()

    if not value:
        raise ProductionRuntimeStateError(
            f"Runtime inspection returned no value for {description}"
        )

    return value


def inspect_runtime_image(
    service_name: str,
    specification: dict[str, Any],
) -> str:
    deploy = specification.get("deploy")

    if not isinstance(deploy, dict):
        raise ProductionRuntimeStateError(
            f"{service_name} does not define deployment metadata"
        )

    runtime_type = deploy.get("type")
    runtime_name = deploy.get("name")

    if not isinstance(runtime_name, str) or not runtime_name:
        raise ProductionRuntimeStateError(
            f"{service_name} does not define a runtime name"
        )

    if runtime_type == "swarm":
        return inspect_value(
            [
                "docker",
                "service",
                "inspect",
                "--format",
                "{{.Spec.TaskTemplate.ContainerSpec.Image}}",
                runtime_name,
            ],
            description=f"Swarm service {runtime_name}",
        )

    if runtime_type == "compose":
        return inspect_value(
            [
                "docker",
                "inspect",
                "--format",
                "{{.Config.Image}}",
                runtime_name,
            ],
            description=f"Compose container {runtime_name}",
        )

    raise ProductionRuntimeStateError(
        f"Unsupported runtime type for {service_name}: {runtime_type}"
    )


def strip_swarm_digest(image: str) -> str:
    image_without_digest, separator, digest = image.partition("@")

    if not separator:
        return image

    if not digest.startswith("sha256:"):
        raise ProductionRuntimeStateError(
            f"Unsupported runtime image digest: {image}"
        )

    return image_without_digest


def release_entry_from_runtime_image(
    service_name: str,
    specification: dict[str, Any],
    runtime_image: str,
) -> dict[str, Any]:
    image_name = specification.get("image")
    deploy = specification.get("deploy")
    kind = specification.get("kind")

    if not isinstance(image_name, str) or not image_name:
        raise ProductionRuntimeStateError(
            f"{service_name} does not define a catalog image"
        )

    if not isinstance(deploy, dict):
        raise ProductionRuntimeStateError(
            f"{service_name} is not deployable"
        )

    normalized_image = strip_swarm_digest(runtime_image)
    marker = f"/{image_name}:"
    registry, separator, source_sha = normalized_image.rpartition(marker)

    if not separator or not registry or not source_sha:
        raise ProductionRuntimeStateError(
            f"Runtime image does not match catalog service {service_name}: {runtime_image}"
        )

    try:
        validate_source_sha(source_sha)
    except ProductionReleaseManifestError as exception:
        raise ProductionRuntimeStateError(
            f"Runtime image is not immutable for {service_name}: {runtime_image}"
        ) from exception

    expected_image = f"{registry}{marker}{source_sha}"

    if normalized_image != expected_image:
        raise ProductionRuntimeStateError(
            f"Runtime image could not be normalized for {service_name}: {runtime_image}"
        )

    return {
        "kind": kind,
        "image": normalized_image,
        "source_sha": source_sha,
        "deploy": deploy,
    }


def manifest_from_runtime_images(
    runtime_images: dict[str, str],
) -> dict[str, Any]:
    catalog = deployable_catalog()

    if set(runtime_images) != set(catalog):
        missing = sorted(set(catalog) - set(runtime_images))
        unexpected = sorted(set(runtime_images) - set(catalog))
        raise ProductionRuntimeStateError(
            "Runtime image set does not match deployable catalog: "
            f"missing={missing}, unexpected={unexpected}"
        )

    entries = {
        service_name: release_entry_from_runtime_image(
            service_name=service_name,
            specification=catalog[service_name],
            runtime_image=runtime_images[service_name],
        )
        for service_name in sorted(catalog)
    }
    manifest = build_manifest(entries)
    validate_manifest(manifest)

    return manifest


def observe_runtime_images() -> dict[str, str]:
    catalog = deployable_catalog()

    return {
        service_name: inspect_runtime_image(
            service_name=service_name,
            specification=specification,
        )
        for service_name, specification in sorted(catalog.items())
    }


def observe_state(tooling_revision: str) -> dict[str, Any]:
    validate_source_sha(tooling_revision)
    manifest = manifest_from_runtime_images(observe_runtime_images())

    return build_state(
        tooling_revision=tooling_revision,
        release_manifest=manifest,
    )


def state_drift(
    recorded_state: dict[str, Any],
    observed_state: dict[str, Any],
) -> list[dict[str, str]]:
    recorded_services = recorded_state["release_manifest"]["services"]
    observed_services = observed_state["release_manifest"]["services"]
    drift: list[dict[str, str]] = []

    for service_name in sorted(recorded_services):
        recorded_image = recorded_services[service_name]["image"]
        observed_image = observed_services[service_name]["image"]

        if recorded_image == observed_image:
            continue

        drift.append(
            {
                "service": service_name,
                "recorded_image": recorded_image,
                "observed_image": observed_image,
            }
        )

    return drift


def write_json(value: dict[str, Any], output: Path | None) -> None:
    serialized = json.dumps(value, indent=2, sort_keys=False) + "\n"

    if output is None:
        sys.stdout.write(serialized)
    else:
        output.write_text(serialized, encoding="utf-8")


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Observe the running Mnemos production images and express them as a "
            "validated per-service deployment state. This tool is read-only with "
            "respect to the production runtime."
        ),
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    observe_parser = subparsers.add_parser("observe")
    observe_parser.add_argument("--tooling-revision", required=True)
    observe_parser.add_argument("--output", type=Path)

    verify_parser = subparsers.add_parser("verify")
    verify_parser.add_argument("--state", type=Path, required=True)

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "observe":
            write_json(
                observe_state(arguments.tooling_revision),
                output=arguments.output,
            )
            return 0

        if arguments.command == "verify":
            recorded_state = read_state(arguments.state)
            observed_state = observe_state(recorded_state["tooling_revision"])
            drift = state_drift(
                recorded_state=recorded_state,
                observed_state=observed_state,
            )

            if drift:
                for entry in drift:
                    print(
                        "Production runtime drift: "
                        f"{entry['service']} recorded={entry['recorded_image']} "
                        f"observed={entry['observed_image']}",
                        file=sys.stderr,
                    )

                return 1

            print("Production runtime matches recorded per-service deployment state")
            return 0
    except (
        OSError,
        ProductionDeploymentStateError,
        ProductionReleaseManifestError,
        ProductionRuntimeStateError,
        ProductionServiceCatalogError,
    ) as exception:
        print(f"Production runtime state observation failed: {exception}", file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
