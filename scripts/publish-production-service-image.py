#!/usr/bin/env python3

from __future__ import annotations

import argparse
import os
import re
import shlex
import subprocess
import sys
from typing import Any

from production_service_catalog import ProductionServiceCatalogError
from production_service_catalog import REPOSITORY_ROOT
from production_service_catalog import service


IMMUTABLE_TAG_PATTERN = re.compile(r"^[0-9a-f]{40}$")


class ProductionServiceImageError(RuntimeError):
    pass


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Build one immutable Mnemos production service image.",
    )
    parser.add_argument("--service", required=True)
    parser.add_argument("--registry", required=True)
    parser.add_argument("--tag", required=True)
    parser.add_argument(
        "--push",
        action="store_true",
        help="Push the image to the configured registry.",
    )
    parser.add_argument(
        "--load",
        action="store_true",
        help="Load the image into the local Docker daemon.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print commands without invoking Gradle or Docker.",
    )
    parser.add_argument(
        "--skip-assemble",
        action="store_true",
        help="Do not run the service-specific Gradle artifact task.",
    )

    return parser.parse_args()


def read_example_environment() -> dict[str, str]:
    path = REPOSITORY_ROOT / ".env.example"
    values: dict[str, str] = {}

    if not path.is_file():
        return values

    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()

        if not line or line.startswith("#") or "=" not in line:
            continue

        key, value = line.split("=", maxsplit=1)
        values[key.strip()] = value.strip()

    return values


def build_argument_values(specification: dict[str, Any]) -> dict[str, str]:
    configured = specification.get("build_args", {})

    if configured is None:
        return {}

    if not isinstance(configured, dict):
        raise ProductionServiceImageError("build_args must be an object")

    example_environment = read_example_environment()
    resolved: dict[str, str] = {}

    for docker_argument, source_name in configured.items():
        if not isinstance(docker_argument, str) or not isinstance(source_name, str):
            raise ProductionServiceImageError(
                "build_args keys and values must be strings"
            )

        value = os.environ.get(source_name, "").strip()

        if not value:
            value = example_environment.get(source_name, "").strip()

        if not value:
            raise ProductionServiceImageError(
                f"{source_name} is required to build {docker_argument}"
            )

        resolved[docker_argument] = value

    return resolved


def validate_arguments(arguments: argparse.Namespace) -> None:
    selected_modes = sum((arguments.push, arguments.load))

    if selected_modes > 1:
        raise ProductionServiceImageError("--push and --load are mutually exclusive")

    if arguments.push and not IMMUTABLE_TAG_PATTERN.fullmatch(arguments.tag):
        raise ProductionServiceImageError(
            "Pushed production images require a full 40-character commit SHA tag"
        )

    if not arguments.registry.strip():
        raise ProductionServiceImageError("--registry must not be blank")


def run_command(command: list[str], dry_run: bool) -> None:
    print(shlex.join(command))

    if dry_run:
        return

    subprocess.run(
        command,
        cwd=REPOSITORY_ROOT,
        check=True,
    )


def assemble_service(
    specification: dict[str, Any],
    skip_assemble: bool,
    dry_run: bool,
) -> None:
    if skip_assemble:
        return

    gradle_task = specification.get("gradle_task")

    if gradle_task is None:
        return

    if not isinstance(gradle_task, str) or not gradle_task:
        raise ProductionServiceImageError("gradle_task must be a non-empty string")

    run_command(
        ["./gradlew", gradle_task, "--no-daemon"],
        dry_run=dry_run,
    )


def build_service_image(
    name: str,
    specification: dict[str, Any],
    arguments: argparse.Namespace,
) -> None:
    image_name = specification.get("image")
    dockerfile_value = specification.get("dockerfile")
    context_value = specification.get("context")

    for field_name, field_value in (
        ("image", image_name),
        ("dockerfile", dockerfile_value),
        ("context", context_value),
    ):
        if not isinstance(field_value, str) or not field_value:
            raise ProductionServiceImageError(
                f"{name}.{field_name} must be a non-empty string"
            )

    dockerfile = REPOSITORY_ROOT / dockerfile_value
    context = REPOSITORY_ROOT / context_value

    if not dockerfile.is_file():
        raise ProductionServiceImageError(
            f"Dockerfile does not exist for {name}: {dockerfile_value}"
        )

    if not context.is_dir():
        raise ProductionServiceImageError(
            f"Build context does not exist for {name}: {context_value}"
        )

    target = f"{arguments.registry.rstrip('/')}/{image_name}:{arguments.tag}"

    command = [
        "docker",
        "buildx",
        "build",
        "--file",
        str(dockerfile),
        "--tag",
        target,
    ]

    for docker_argument, value in build_argument_values(specification).items():
        command.extend(
            [
                "--build-arg",
                f"{docker_argument}={value}",
            ]
        )

    if arguments.push:
        command.append("--push")
    elif arguments.load:
        command.append("--load")

    command.append(str(context))

    run_command(
        command,
        dry_run=arguments.dry_run,
    )

    print(f"Prepared production image for {name}: {target}")


def main() -> int:
    arguments = parse_arguments()

    try:
        validate_arguments(arguments)
        specification = service(arguments.service)

        assemble_service(
            specification=specification,
            skip_assemble=arguments.skip_assemble,
            dry_run=arguments.dry_run,
        )
        build_service_image(
            name=arguments.service,
            specification=specification,
            arguments=arguments,
        )
    except (
        ProductionServiceCatalogError,
        ProductionServiceImageError,
        subprocess.CalledProcessError,
    ) as exception:
        print(f"Production service image build failed: {exception}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
