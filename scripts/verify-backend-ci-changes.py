#!/usr/bin/env python3

from __future__ import annotations

import sys
from pathlib import Path

from resolve_backend_ci_changes import resolve_changes


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def verify_integration_test_tags() -> None:
    for group in ("apps", "libraries"):
        for path in REPOSITORY_ROOT.glob(f"{group}/*/src/test/**/*.kt"):
            content = path.read_text(encoding="utf-8")

            if "@SpringBootTest" not in content and "@Testcontainers" not in content:
                continue

            require(
                '@Tag("integration")' in content,
                f"Heavy integration test must be tagged integration: {path.relative_to(REPOSITORY_ROOT)}",
            )


def main() -> int:
    app_result = resolve_changes([
        "apps/account-service/src/main/kotlin/example/Account.kt",
    ])

    require(
        ":apps:account-service:fastCheck" in app_result["verification_tasks"],
        "Direct application source change must use fastCheck",
    )
    require(app_result["fast_path"], "Direct application source change must use the fast path")
    require(not app_result["full_backend"], "Direct application change must stay scoped")
    require(
        not app_result["dependency_scan_required"],
        "Source-only change must not force dependency scan",
    )
    require(
        not app_result["stack_smoke_required"],
        "Source-only application change must not require full Stack Smoke",
    )
    require(
        not app_result["integration_verify_required"],
        "Ordinary application business logic must not require Integration Verify",
    )

    persistence_result = resolve_changes([
        "apps/account-service/src/main/kotlin/com/mnemos/account/adapter/out/persistence/AccountRepository.kt",
    ])
    require(
        persistence_result["integration_verify_required"],
        "Persistence changes must require Integration Verify",
    )
    require(
        not persistence_result["stack_smoke_required"],
        "Persistence-only Kotlin changes must not require full Stack Smoke",
    )

    app_test_result = resolve_changes([
        "apps/account-service/src/test/kotlin/example/AccountTest.kt",
    ])
    require(
        not app_test_result["fast_path"],
        "Test-source changes must run the full project check",
    )
    require(
        ":apps:account-service:check" in app_test_result["verification_tasks"],
        "Test-source changes must use the full project check",
    )

    app_resource_result = resolve_changes([
        "apps/account-service/src/main/resources/db/migration/V99__example.sql",
    ])
    require(
        app_resource_result["stack_smoke_required"],
        "Runtime resources and migrations must require Stack Smoke",
    )
    require(
        not app_resource_result["fast_path"],
        "Runtime resources must not use the fast path",
    )
    require(
        app_resource_result["integration_verify_required"],
        "Runtime resources and migrations must require Integration Verify",
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
    require(
        contract_result["stack_smoke_required"],
        "Shared contracts must require Stack Smoke",
    )
    require(
        contract_result["integration_verify_required"],
        "Shared contracts must require Integration Verify",
    )

    library_result = resolve_changes([
        "libraries/outbox-starter/src/main/kotlin/example/Repository.kt",
    ])
    require(
        library_result["integration_verify_required"],
        "Shared library changes must require Integration Verify",
    )

    build_result = resolve_changes([
        "apps/account-service/build.gradle.kts",
    ])
    require(
        build_result["dependency_scan_required"],
        "Project dependency descriptor change must request dependency scan",
    )
    require(
        build_result["stack_smoke_required"],
        "Application build descriptor changes must require Stack Smoke",
    )

    global_result = resolve_changes([
        "gradle/libs.versions.toml",
    ])
    require(global_result["full_backend"], "Global Gradle change must verify the full backend")
    require(
        global_result["dependency_scan_required"],
        "Global Gradle change must request dependency scan",
    )
    require(
        global_result["stack_smoke_required"],
        "Global Gradle change must require Stack Smoke",
    )

    infrastructure_result = resolve_changes([
        "infrastructure/postgres/init-service-database-access.sh",
    ])
    require(
        infrastructure_result["stack_smoke_required"],
        "Infrastructure changes must require Stack Smoke",
    )
    require(
        not infrastructure_result["fast_path"],
        "Infrastructure changes must never use the source fast path",
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
    require(
        not docs_result["stack_smoke_required"],
        "Documentation-only change must not require Stack Smoke",
    )
    require(
        not docs_result["integration_verify_required"],
        "Documentation-only change must not require Integration Verify",
    )

    verify_integration_test_tags()

    print("Backend CI affected-project, fast-path, and integration boundaries verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(f"Backend CI affected-project verification failed: {exception}", file=sys.stderr)
        raise SystemExit(1)
