#!/usr/bin/env python3

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any

from production_service_catalog import ProductionServiceCatalogError
from production_service_catalog import services as catalog_services


SCHEMA_VERSION = 1
SOURCE_SHA_PATTERN = re.compile(r"^[0-9a-f]{40}$")


class ProductionReleaseManifestError(RuntimeError):
    pass


def validate_source_sha(value: str) -> str:
    if not SOURCE_SHA_PATTERN.fullmatch(value):
        raise ProductionReleaseManifestError(
            f"Source SHA must contain exactly 40 lowercase hexadecimal characters: {value}"
        )

    return value


def deployable_services() -> dict[str, dict[str, Any]]:
    return {
        name: specification
        for name, specification in catalog_services().items()
        if isinstance(specification.get("deploy"), dict)
    }


def service_entry(
    name: str,
    specification: dict[str, Any],
    registry: str,
    source_sha: str,
) -> dict[str, Any]:
    image_name = specification.get("image")
    deploy = specification.get("deploy")

    if not isinstance(image_name, str) or not image_name:
        raise ProductionReleaseManifestError(f"{name} does not define an image")

    if not isinstance(deploy, dict):
        raise ProductionReleaseManifestError(
            f"{name} is not deployable and cannot be added to a release manifest"
        )

    return {
        "kind": specification.get("kind"),
        "image": f"{registry.rstrip('/')}/{image_name}:{source_sha}",
        "source_sha": source_sha,
        "deploy": deploy,
    }


def manifest_id(services: dict[str, Any]) -> str:
    canonical = json.dumps(
        services,
        separators=(",", ":"),
        sort_keys=True,
    ).encode("utf-8")

    return "sha256:" + hashlib.sha256(canonical).hexdigest()


def build_manifest(services: dict[str, Any]) -> dict[str, Any]:
    ordered_services = {
        name: services[name]
        for name in sorted(services)
    }

    return {
        "schema_version": SCHEMA_VERSION,
        "manifest_id": manifest_id(ordered_services),
        "services": ordered_services,
    }


def create_manifest(
    registry: str,
    source_sha: str,
) -> dict[str, Any]:
    validate_source_sha(source_sha)

    if not registry.strip():
        raise ProductionReleaseManifestError("Registry must not be blank")

    entries = {
        name: service_entry(
            name=name,
            specification=specification,
            registry=registry,
            source_sha=source_sha,
        )
        for name, specification in deployable_services().items()
    }

    return build_manifest(entries)


def read_manifest(path: Path) -> dict[str, Any]:
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ProductionReleaseManifestError(
            f"Release manifest does not exist: {path}"
        ) from exception
    except json.JSONDecodeError as exception:
        raise ProductionReleaseManifestError(
            f"Release manifest is invalid JSON: {exception}"
        ) from exception

    validate_manifest(manifest)
    return manifest


def validate_manifest(manifest: dict[str, Any]) -> None:
    if not isinstance(manifest, dict):
        raise ProductionReleaseManifestError("Release manifest must be an object")

    if manifest.get("schema_version") != SCHEMA_VERSION:
        raise ProductionReleaseManifestError("Unsupported release manifest schema")

    services = manifest.get("services")

    if not isinstance(services, dict) or not services:
        raise ProductionReleaseManifestError(
            "Release manifest must contain deployable services"
        )

    catalog = deployable_services()

    if set(services) != set(catalog):
        raise ProductionReleaseManifestError(
            "Release manifest service set does not match the deployable service catalog"
        )

    for name, entry in services.items():
        if not isinstance(entry, dict):
            raise ProductionReleaseManifestError(
                f"{name} release entry must be an object"
            )

        source_sha = entry.get("source_sha")
        image = entry.get("image")
        deploy = entry.get("deploy")
        kind = entry.get("kind")

        if not isinstance(source_sha, str):
            raise ProductionReleaseManifestError(
                f"{name}.source_sha must be a string"
            )

        validate_source_sha(source_sha)

        expected_image_suffix = f"/{catalog[name]['image']}:{source_sha}"

        if not isinstance(image, str) or not image.endswith(expected_image_suffix):
            raise ProductionReleaseManifestError(
                f"{name}.image does not match its catalog image and source SHA"
            )

        if kind != catalog[name].get("kind"):
            raise ProductionReleaseManifestError(
                f"{name}.kind does not match the service catalog"
            )

        if deploy != catalog[name].get("deploy"):
            raise ProductionReleaseManifestError(
                f"{name}.deploy does not match the service catalog"
            )

    expected_id = manifest_id(
        {
            name: services[name]
            for name in sorted(services)
        }
    )

    if manifest.get("manifest_id") != expected_id:
        raise ProductionReleaseManifestError(
            "Release manifest ID does not match its service contents"
        )


def parse_assignment(value: str) -> tuple[str, str]:
    name, separator, source_sha = value.partition("=")

    if not separator or not name or not source_sha:
        raise argparse.ArgumentTypeError(
            "--set must use SERVICE=FULL_COMMIT_SHA"
        )

    try:
        validate_source_sha(source_sha)
    except ProductionReleaseManifestError as exception:
        raise argparse.ArgumentTypeError(str(exception)) from exception

    return name, source_sha


def update_manifest(
    base: dict[str, Any],
    assignments: list[tuple[str, str]],
) -> dict[str, Any]:
    validate_manifest(base)

    if not assignments:
        raise ProductionReleaseManifestError(
            "At least one service override is required"
        )

    catalog = deployable_services()
    updated_services = json.loads(json.dumps(base["services"]))

    for name, source_sha in assignments:
        specification = catalog.get(name)

        if specification is None:
            available = ", ".join(sorted(catalog))
            raise ProductionReleaseManifestError(
                f"Service '{name}' is not deployable. Available services: {available}"
            )

        current_image = updated_services[name]["image"]
        image_name = specification["image"]
        registry_prefix, separator, _ = current_image.rpartition(f"/{image_name}:")

        if not separator or not registry_prefix:
            raise ProductionReleaseManifestError(
                f"Cannot resolve registry from current image for {name}: {current_image}"
            )

        updated_services[name] = service_entry(
            name=name,
            specification=specification,
            registry=registry_prefix,
            source_sha=source_sha,
        )

    return build_manifest(updated_services)


def manifest_diff(
    current: dict[str, Any],
    target: dict[str, Any],
) -> list[dict[str, Any]]:
    validate_manifest(current)
    validate_manifest(target)

    changes: list[dict[str, Any]] = []

    for name in sorted(target["services"]):
        current_entry = current["services"][name]
        target_entry = target["services"][name]

        if current_entry == target_entry:
            continue

        changes.append(
            {
                "service": name,
                "deploy": target_entry["deploy"],
                "from_image": current_entry["image"],
                "to_image": target_entry["image"],
                "from_source_sha": current_entry["source_sha"],
                "to_source_sha": target_entry["source_sha"],
            }
        )

    return changes


def write_json(value: dict[str, Any] | list[dict[str, Any]], path: Path | None) -> None:
    serialized = json.dumps(value, indent=2, sort_keys=False) + "\n"

    if path is None:
        sys.stdout.write(serialized)
    else:
        path.write_text(serialized, encoding="utf-8")


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create, update, validate, and diff Mnemos production release manifests.",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    create_parser = subparsers.add_parser("create")
    create_parser.add_argument("--registry", required=True)
    create_parser.add_argument("--source-sha", required=True)
    create_parser.add_argument("--output", type=Path)

    update_parser = subparsers.add_parser("update")
    update_parser.add_argument("--input", type=Path, required=True)
    update_parser.add_argument(
        "--set",
        dest="assignments",
        action="append",
        type=parse_assignment,
        required=True,
    )
    update_parser.add_argument("--output", type=Path)

    validate_parser = subparsers.add_parser("validate")
    validate_parser.add_argument("--input", type=Path, required=True)

    diff_parser = subparsers.add_parser("diff")
    diff_parser.add_argument("--current", type=Path, required=True)
    diff_parser.add_argument("--target", type=Path, required=True)
    diff_parser.add_argument(
        "--format",
        choices=("json", "names"),
        default="json",
    )

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "create":
            write_json(
                create_manifest(
                    registry=arguments.registry,
                    source_sha=arguments.source_sha,
                ),
                path=arguments.output,
            )
            return 0

        if arguments.command == "update":
            base = read_manifest(arguments.input)
            write_json(
                update_manifest(
                    base=base,
                    assignments=arguments.assignments,
                ),
                path=arguments.output,
            )
            return 0

        if arguments.command == "validate":
            read_manifest(arguments.input)
            print("Production release manifest verified")
            return 0

        if arguments.command == "diff":
            current = read_manifest(arguments.current)
            target = read_manifest(arguments.target)
            changes = manifest_diff(
                current=current,
                target=target,
            )

            if arguments.format == "names":
                for change in changes:
                    print(change["service"])
            else:
                write_json(changes, path=None)

            return 0
    except (
        ProductionReleaseManifestError,
        ProductionServiceCatalogError,
    ) as exception:
        print(f"Production release manifest failed: {exception}", file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
