#!/usr/bin/env python3

from __future__ import annotations

import argparse
import subprocess
import sys


class ProductionRuntimeBaselineError(RuntimeError):
    pass


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
        raise ProductionRuntimeBaselineError(
            f"Could not inspect {description}{detail}"
        ) from exception

    value = result.stdout.strip()

    if not value:
        raise ProductionRuntimeBaselineError(
            f"Runtime inspection returned no value for {description}"
        )

    return value


def swarm_image(runtime_name: str) -> str:
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


def compose_image(runtime_name: str) -> str:
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


def image_matches(
    runtime_type: str,
    actual_image: str,
    expected_image: str,
) -> bool:
    if runtime_type == "swarm":
        return actual_image in {
            expected_image,
        } or actual_image.startswith(expected_image + "@sha256:")

    if runtime_type == "compose":
        return actual_image == expected_image

    raise ProductionRuntimeBaselineError(
        f"Unsupported production runtime type: {runtime_type}"
    )


def verify_runtime_baseline(
    runtime_type: str,
    runtime_name: str,
    expected_image: str,
) -> str:
    if runtime_type == "swarm":
        actual_image = swarm_image(runtime_name)
    elif runtime_type == "compose":
        actual_image = compose_image(runtime_name)
    else:
        raise ProductionRuntimeBaselineError(
            f"Unsupported production runtime type: {runtime_type}"
        )

    if not image_matches(
        runtime_type=runtime_type,
        actual_image=actual_image,
        expected_image=expected_image,
    ):
        raise ProductionRuntimeBaselineError(
            "Production runtime drift detected: "
            f"runtime={runtime_name} expected={expected_image} actual={actual_image}"
        )

    return actual_image


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Verify that one production runtime target still uses the exact image "
            "recorded in the current deployment state."
        ),
    )
    parser.add_argument(
        "--runtime-type",
        choices=("swarm", "compose"),
        required=True,
    )
    parser.add_argument("--runtime-name", required=True)
    parser.add_argument("--expected-image", required=True)

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        actual_image = verify_runtime_baseline(
            runtime_type=arguments.runtime_type,
            runtime_name=arguments.runtime_name,
            expected_image=arguments.expected_image,
        )
    except ProductionRuntimeBaselineError as exception:
        print(f"Production runtime baseline verification failed: {exception}", file=sys.stderr)
        return 1

    print(
        "Production runtime baseline verified: "
        f"{arguments.runtime_name} -> {actual_image}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
