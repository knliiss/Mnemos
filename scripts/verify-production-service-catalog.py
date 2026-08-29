#!/usr/bin/env python3

from __future__ import annotations

import ast
import sys
from typing import Any

from production_service_catalog import ProductionServiceCatalogError
from production_service_catalog import REPOSITORY_ROOT
from production_service_catalog import load_catalog


RENDERER_PATH = REPOSITORY_ROOT / "scripts" / "render-production-stack.py"


class ProductionServiceCatalogVerificationError(RuntimeError):
    pass


def application_services_from_renderer() -> tuple[str, ...]:
    module = ast.parse(RENDERER_PATH.read_text(encoding="utf-8"))

    for node in module.body:
        if not isinstance(node, ast.Assign):
            continue

        for target in node.targets:
            if isinstance(target, ast.Name) and target.id == "APPLICATION_SERVICES":
                value = ast.literal_eval(node.value)

                if not isinstance(value, tuple) or not all(
                    isinstance(item, str) for item in value
                ):
                    raise ProductionServiceCatalogVerificationError(
                        "APPLICATION_SERVICES must be a tuple of strings"
                    )

                return value

    raise ProductionServiceCatalogVerificationError(
        "APPLICATION_SERVICES was not found in render-production-stack.py"
    )


def application_services_from_dockerfiles() -> tuple[str, ...]:
    dockerfiles = sorted((REPOSITORY_ROOT / "apps").glob("*/Dockerfile"))

    return tuple(path.parent.name for path in dockerfiles)


def verify_required_fields(
    name: str,
    specification: dict[str, Any],
) -> None:
    for field_name in ("kind", "image", "dockerfile", "context"):
        field_value = specification.get(field_name)

        if not isinstance(field_value, str) or not field_value:
            raise ProductionServiceCatalogVerificationError(
                f"{name}.{field_name} must be a non-empty string"
            )

    if specification["kind"] not in {"application", "infrastructure"}:
        raise ProductionServiceCatalogVerificationError(
            f"{name}.kind is unsupported: {specification['kind']}"
        )

    dockerfile = REPOSITORY_ROOT / specification["dockerfile"]
    context = REPOSITORY_ROOT / specification["context"]

    if not dockerfile.is_file():
        raise ProductionServiceCatalogVerificationError(
            f"{name} Dockerfile does not exist: {specification['dockerfile']}"
        )

    if not context.is_dir():
        raise ProductionServiceCatalogVerificationError(
            f"{name} build context does not exist: {specification['context']}"
        )

    gradle_task = specification.get("gradle_task")

    if gradle_task is not None and (
        not isinstance(gradle_task, str) or not gradle_task.startswith(":")
    ):
        raise ProductionServiceCatalogVerificationError(
            f"{name}.gradle_task must be an absolute Gradle task path"
        )

    build_args = specification.get("build_args", {})

    if not isinstance(build_args, dict) or not all(
        isinstance(key, str) and isinstance(value, str)
        for key, value in build_args.items()
    ):
        raise ProductionServiceCatalogVerificationError(
            f"{name}.build_args must map Docker arguments to environment names"
        )


def verify_application_service(
    name: str,
    specification: dict[str, Any],
) -> None:
    expected_image = f"mnemos-{name}"
    expected_dockerfile = f"apps/{name}/Dockerfile"
    expected_gradle_task = f":apps:{name}:bootJar"
    expected_runtime_name = f"mnemos_{name}"

    if specification["image"] != expected_image:
        raise ProductionServiceCatalogVerificationError(
            f"{name} image must be {expected_image}"
        )

    if specification["dockerfile"] != expected_dockerfile:
        raise ProductionServiceCatalogVerificationError(
            f"{name} Dockerfile must be {expected_dockerfile}"
        )

    if specification.get("gradle_task") != expected_gradle_task:
        raise ProductionServiceCatalogVerificationError(
            f"{name} Gradle task must be {expected_gradle_task}"
        )

    deploy = specification.get("deploy")

    if not isinstance(deploy, dict):
        raise ProductionServiceCatalogVerificationError(
            f"{name} must define Swarm deployment metadata"
        )

    if deploy.get("type") != "swarm" or deploy.get("name") != expected_runtime_name:
        raise ProductionServiceCatalogVerificationError(
            f"{name} runtime must be Swarm service {expected_runtime_name}"
        )


def verify_catalog() -> None:
    catalog = load_catalog()
    raw_services = catalog["services"]

    if not isinstance(raw_services, dict):
        raise ProductionServiceCatalogVerificationError(
            "Catalog services must be an object"
        )

    configured_services: dict[str, dict[str, Any]] = {}

    for name, specification in raw_services.items():
        if not isinstance(name, str) or not name:
            raise ProductionServiceCatalogVerificationError(
                "Catalog service names must be non-empty strings"
            )

        if not isinstance(specification, dict):
            raise ProductionServiceCatalogVerificationError(
                f"{name} specification must be an object"
            )

        verify_required_fields(
            name=name,
            specification=specification,
        )
        configured_services[name] = specification

    images = [specification["image"] for specification in configured_services.values()]

    if len(images) != len(set(images)):
        raise ProductionServiceCatalogVerificationError(
            "Production image names must be unique"
        )

    configured_applications = tuple(
        sorted(
            name
            for name, specification in configured_services.items()
            if specification["kind"] == "application"
        )
    )
    renderer_applications = tuple(sorted(application_services_from_renderer()))
    dockerfile_applications = tuple(sorted(application_services_from_dockerfiles()))

    if configured_applications != renderer_applications:
        raise ProductionServiceCatalogVerificationError(
            "Application service catalog does not match render-production-stack.py: "
            f"catalog={configured_applications}, renderer={renderer_applications}"
        )

    if configured_applications != dockerfile_applications:
        raise ProductionServiceCatalogVerificationError(
            "Application service catalog does not match apps/*/Dockerfile: "
            f"catalog={configured_applications}, dockerfiles={dockerfile_applications}"
        )

    for name in configured_applications:
        verify_application_service(
            name=name,
            specification=configured_services[name],
        )


def main() -> int:
    try:
        verify_catalog()
    except (
        ProductionServiceCatalogError,
        ProductionServiceCatalogVerificationError,
    ) as exception:
        print(f"Production service catalog verification failed: {exception}", file=sys.stderr)
        return 1

    print("Production service catalog verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
