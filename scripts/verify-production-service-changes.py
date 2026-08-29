#!/usr/bin/env python3

from __future__ import annotations

import sys

from resolve_production_service_changes import resolve_changes


def fail(message: str) -> None:
    raise AssertionError(message)


def verify_direct_service_change() -> None:
    result = resolve_changes(
        ["apps/account-service/src/main/kotlin/com/mnemos/account/AccountServiceApplication.kt"]
    )

    if result["image_services"] != ["account-service"]:
        fail(f"Direct account-service change was not scoped: {result}")


def verify_shared_library_change() -> None:
    result = resolve_changes(
        ["libraries/common-core/src/main/kotlin/com/mnemos/common/Example.kt"]
    )

    if "account-service" not in result["image_services"]:
        fail(f"common-core change did not affect account-service: {result}")

    if len(result["image_services"]) < 2:
        fail(f"common-core change should affect multiple dependent services: {result}")


def verify_keycloak_change() -> None:
    extension_result = resolve_changes(
        [
            "keycloak-extensions/telegram-authenticator/src/main/java/"
            "com/mnemos/keycloak/telegram/TelegramAuthenticator.java"
        ]
    )

    if extension_result["image_services"] != ["keycloak"]:
        fail(f"Telegram authenticator change was not scoped to Keycloak: {extension_result}")

    theme_result = resolve_changes(
        ["infrastructure/keycloak/themes/mnemos/login/login.ftl"]
    )

    if "keycloak" not in theme_result["image_services"]:
        fail(f"Keycloak theme change did not rebuild Keycloak: {theme_result}")


def verify_runtime_only_change() -> None:
    result = resolve_changes(
        ["infrastructure/compose/apps/compose.account-service.yml"]
    )

    if result["image_services"]:
        fail(f"Runtime Compose change unexpectedly rebuilds images: {result}")

    if result["runtime_services"] != ["account-service"]:
        fail(f"Runtime Compose change did not target account-service: {result}")


def verify_global_build_change() -> None:
    result = resolve_changes(["gradle/libs.versions.toml"])

    if "account-service" not in result["image_services"]:
        fail(f"Global Gradle change did not affect application images: {result}")

    if "keycloak" not in result["image_services"]:
        fail(f"Global Gradle change did not affect Keycloak extension image: {result}")


def verify_infrastructure_image_change() -> None:
    result = resolve_changes(["infrastructure/kafka/configure-topics.sh"])

    expected = {"kafka-init", "kafka-tls-init"}

    if not expected.issubset(set(result["image_services"])):
        fail(f"Kafka infrastructure change did not affect both Kafka images: {result}")


def verify_full_runtime_reconcile() -> None:
    result = resolve_changes(["scripts/render-production-stack.py"])

    if not result["requires_full_runtime_reconcile"]:
        fail(f"Production renderer change did not require full runtime reconcile: {result}")


def verify_unrelated_change() -> None:
    result = resolve_changes(["README.md"])

    if result["image_services"]:
        fail(f"README change unexpectedly rebuilds production images: {result}")

    if result["runtime_services"]:
        fail(f"README change unexpectedly updates production runtime: {result}")

    if result["manual_review_paths"]:
        fail(f"README change unexpectedly requires deployment review: {result}")


def main() -> int:
    verify_direct_service_change()
    verify_shared_library_change()
    verify_keycloak_change()
    verify_runtime_only_change()
    verify_global_build_change()
    verify_infrastructure_image_change()
    verify_full_runtime_reconcile()
    verify_unrelated_change()

    print("Production affected-service resolution verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(f"Production affected-service verification failed: {exception}", file=sys.stderr)
        raise SystemExit(1)
