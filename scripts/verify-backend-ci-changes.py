#!/usr/bin/env python3

from __future__ import annotations

import sys

from resolve_backend_ci_changes import resolve_changes


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> int:
    app_result = resolve_changes([
        "apps/account-service/src/main/kotlin/example/Account.kt",
    ])

    require(
        ":apps:account-service:check" in app_result["check_tasks"],
        "Direct account-service change must verify account-service",
    )
    require(
        not app_result["full_backend"],
        "Direct application change must stay scoped",
    )
    require(
        not app_result["dependency_scan_required"],
        "Source-only change must not force dependency scan",
    )

    contract_result = resolve_changes([
        "contracts/account-contracts/src/main/kotlin/example/Contract.kt",
    ])

    require(
        ":contracts:account-contracts:check" in contract_result["check_tasks"],
        "Changed contract must verify itself",
    )
    require(
        len(contract_result["check_tasks"]) > 1,
        "Changed contract must verify transitive dependents",
    )

    build_result = resolve_changes([
        "apps/account-service/build.gradle.kts",
    ])

    require(
        build_result["dependency_scan_required"],
        "Project dependency descriptor change must request dependency scan",
    )

    global_result = resolve_changes([
        "gradle/libs.versions.toml",
    ])

    require(
        global_result["full_backend"],
        "Global Gradle change must verify the full backend",
    )
    require(
        global_result["dependency_scan_required"],
        "Global Gradle change must request dependency scan",
    )

    docs_result = resolve_changes([
        "docs/production-emergency-hotfix.md",
    ])

    require(
        docs_result["check_tasks"] == [],
        "Documentation-only change must not schedule Gradle checks",
    )
    require(
        not docs_result["dependency_scan_required"],
        "Documentation-only change must not request dependency scan",
    )

    print("Backend CI affected-project resolution verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(f"Backend CI affected-project verification failed: {exception}", file=sys.stderr)
        raise SystemExit(1)
