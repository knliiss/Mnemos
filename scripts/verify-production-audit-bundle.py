#!/usr/bin/env python3

from __future__ import annotations

import json
import tempfile
from pathlib import Path

from production_audit_bundle import ProductionAuditBundleError
from production_audit_bundle import validate_manifest
from production_audit_bundle import write_manifest


def fail(message: str) -> None:
    raise AssertionError(message)


def verify_manifest_detects_tampering() -> None:
    with tempfile.TemporaryDirectory() as directory:
        bundle_dir = Path(directory)
        first = bundle_dir / "deployment-plan.json"
        second = bundle_dir / "transition.json"

        first.write_text('{"plan":"one"}\n', encoding="utf-8")
        second.write_text('{"transition":"one"}\n', encoding="utf-8")

        manifest_path = write_manifest(
            directory=bundle_dir,
            bundle_kind="release-plan",
        )
        manifest = validate_manifest(
            directory=bundle_dir,
            expected_kind="release-plan",
        )

        names = [entry["name"] for entry in manifest["files"]]

        if names != ["deployment-plan.json", "transition.json"]:
            fail("Audit manifest file ordering must be deterministic")

        stored = json.loads(manifest_path.read_text(encoding="utf-8"))

        if any(not entry["sha256"].startswith("sha256:") for entry in stored["files"]):
            fail("Audit manifest must store SHA-256 digests")

        first.write_text('{"plan":"tampered"}\n', encoding="utf-8")

        try:
            validate_manifest(
                directory=bundle_dir,
                expected_kind="release-plan",
            )
        except ProductionAuditBundleError:
            pass
        else:
            fail("Audit bundle verification must reject modified files")


def verify_manifest_detects_untracked_files() -> None:
    with tempfile.TemporaryDirectory() as directory:
        bundle_dir = Path(directory)
        (bundle_dir / "target-state.json").write_text("{}\n", encoding="utf-8")

        write_manifest(
            directory=bundle_dir,
            bundle_kind="hotfix-plan",
        )
        (bundle_dir / "unexpected.json").write_text("{}\n", encoding="utf-8")

        try:
            validate_manifest(
                directory=bundle_dir,
                expected_kind="hotfix-plan",
            )
        except ProductionAuditBundleError:
            return

        fail("Audit bundle verification must reject untracked files")


def main() -> int:
    verify_manifest_detects_tampering()
    verify_manifest_detects_untracked_files()

    print("Production audit bundle integrity verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
