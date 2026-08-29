#!/usr/bin/env python3

from __future__ import annotations

import argparse
import hashlib
import json
import sys
from pathlib import Path
from typing import Any


SCHEMA_VERSION = 1
AUDIT_MANIFEST_NAME = "audit-manifest.json"


class ProductionAuditBundleError(RuntimeError):
    pass


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()

    with path.open("rb") as file:
        for chunk in iter(lambda: file.read(1024 * 1024), b""):
            digest.update(chunk)

    return "sha256:" + digest.hexdigest()


def bundle_files(directory: Path) -> list[Path]:
    if not directory.is_dir():
        raise ProductionAuditBundleError(
            f"Audit bundle directory does not exist: {directory}"
        )

    manifest_path = directory / AUDIT_MANIFEST_NAME
    files = [
        path
        for path in directory.rglob("*")
        if path.is_file() and path != manifest_path
    ]

    if not files:
        raise ProductionAuditBundleError("Audit bundle must contain at least one file")

    return sorted(
        files,
        key=lambda path: path.relative_to(directory).as_posix(),
    )


def relative_name(directory: Path, path: Path) -> str:
    return path.relative_to(directory).as_posix()


def validate_relative_name(name: str) -> None:
    path = Path(name)

    if not name or path.is_absolute() or ".." in path.parts:
        raise ProductionAuditBundleError(
            f"Audit manifest file name is invalid: {name}"
        )

    if path.as_posix() != name:
        raise ProductionAuditBundleError(
            f"Audit manifest file name must use normalized POSIX separators: {name}"
        )


def build_manifest(directory: Path, bundle_kind: str) -> dict[str, Any]:
    bundle_kind = bundle_kind.strip()

    if not bundle_kind:
        raise ProductionAuditBundleError("Audit bundle kind must not be blank")

    files = bundle_files(directory)

    return {
        "schema_version": SCHEMA_VERSION,
        "bundle_kind": bundle_kind,
        "files": [
            {
                "name": relative_name(directory, path),
                "sha256": sha256_file(path),
                "size_bytes": path.stat().st_size,
            }
            for path in files
        ],
    }


def write_manifest(directory: Path, bundle_kind: str) -> Path:
    manifest = build_manifest(
        directory=directory,
        bundle_kind=bundle_kind,
    )
    output = directory / AUDIT_MANIFEST_NAME
    output.write_text(
        json.dumps(manifest, indent=2, sort_keys=False) + "\n",
        encoding="utf-8",
    )
    return output


def read_manifest(directory: Path) -> dict[str, Any]:
    path = directory / AUDIT_MANIFEST_NAME

    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ProductionAuditBundleError(
            f"Audit manifest does not exist: {path}"
        ) from exception
    except json.JSONDecodeError as exception:
        raise ProductionAuditBundleError(
            f"Audit manifest is invalid JSON: {exception}"
        ) from exception

    if not isinstance(manifest, dict):
        raise ProductionAuditBundleError("Audit manifest must be an object")

    return manifest


def validate_manifest(directory: Path, expected_kind: str | None = None) -> dict[str, Any]:
    manifest = read_manifest(directory)

    if manifest.get("schema_version") != SCHEMA_VERSION:
        raise ProductionAuditBundleError("Unsupported audit manifest schema")

    bundle_kind = manifest.get("bundle_kind")

    if not isinstance(bundle_kind, str) or not bundle_kind.strip():
        raise ProductionAuditBundleError("Audit manifest bundle_kind is invalid")

    if expected_kind is not None and bundle_kind != expected_kind:
        raise ProductionAuditBundleError(
            f"Audit bundle kind mismatch: expected={expected_kind} actual={bundle_kind}"
        )

    entries = manifest.get("files")

    if not isinstance(entries, list) or not entries:
        raise ProductionAuditBundleError("Audit manifest file list is invalid")

    expected_names: set[str] = set()

    for entry in entries:
        if not isinstance(entry, dict):
            raise ProductionAuditBundleError("Audit manifest file entry is invalid")

        name = entry.get("name")
        expected_digest = entry.get("sha256")
        expected_size = entry.get("size_bytes")

        if not isinstance(name, str):
            raise ProductionAuditBundleError("Audit manifest file name must be a string")

        validate_relative_name(name)

        if name == AUDIT_MANIFEST_NAME:
            raise ProductionAuditBundleError("Audit manifest must not hash itself")

        if name in expected_names:
            raise ProductionAuditBundleError(f"Duplicate audit manifest file entry: {name}")

        expected_names.add(name)
        path = directory / Path(name)

        if not path.is_file():
            raise ProductionAuditBundleError(f"Audit bundle file is missing: {name}")

        actual_size = path.stat().st_size

        if expected_size != actual_size:
            raise ProductionAuditBundleError(
                f"Audit bundle size mismatch for {name}: expected={expected_size} actual={actual_size}"
            )

        actual_digest = sha256_file(path)

        if expected_digest != actual_digest:
            raise ProductionAuditBundleError(
                f"Audit bundle digest mismatch for {name}: expected={expected_digest} actual={actual_digest}"
            )

    actual_names = {
        relative_name(directory, path)
        for path in bundle_files(directory)
    }

    if actual_names != expected_names:
        unexpected = sorted(actual_names - expected_names)
        missing = sorted(expected_names - actual_names)
        raise ProductionAuditBundleError(
            f"Audit bundle file set mismatch: unexpected={unexpected} missing={missing}"
        )

    return manifest


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create or verify a tamper-evident Mnemos production audit bundle manifest.",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    create_parser = subparsers.add_parser("create")
    create_parser.add_argument("--directory", type=Path, required=True)
    create_parser.add_argument("--kind", required=True)

    verify_parser = subparsers.add_parser("verify")
    verify_parser.add_argument("--directory", type=Path, required=True)
    verify_parser.add_argument("--kind")

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "create":
            output = write_manifest(
                directory=arguments.directory,
                bundle_kind=arguments.kind,
            )
            validate_manifest(
                directory=arguments.directory,
                expected_kind=arguments.kind,
            )
            print(f"Production audit manifest created: {output}")
            return 0

        if arguments.command == "verify":
            manifest = validate_manifest(
                directory=arguments.directory,
                expected_kind=arguments.kind,
            )
            print(
                "Production audit bundle verified: "
                f"{manifest['bundle_kind']} ({len(manifest['files'])} files)"
            )
            return 0
    except (OSError, ProductionAuditBundleError) as exception:
        print(f"Production audit bundle failed: {exception}", file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
