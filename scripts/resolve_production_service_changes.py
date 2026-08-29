#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict, deque
from pathlib import Path
from typing import Any

from production_service_catalog import REPOSITORY_ROOT
from production_service_catalog import services as catalog_services


PROJECT_DEPENDENCY_PATTERN = re.compile(
    r"""project\(\s*[\"'](?P<project>:[^\"']+)[\"']\s*\)"""
)
GLOBAL_GRADLE_PATHS = {
    "build.gradle.kts",
    "gradle.properties",
    "settings.gradle.kts",
    "gradlew",
    "gradlew.bat",
}
GLOBAL_GRADLE_PREFIXES = (
    "build-logic/",
    "gradle/",
)
FULL_RUNTIME_RECONCILE_PATHS = {
    "infrastructure/production/compose.app-overrides.yml",
    "scripts/render-production-stack.py",
    "scripts/normalize-production-swarm-stack.py",
}
FULL_RUNTIME_RECONCILE_PREFIXES = (
    "infrastructure/production/",
)
DEPLOYMENT_SENSITIVE_PREFIXES = (
    ".github/workflows/production",
    "scripts/deploy-production",
    "scripts/start-production",
    "scripts/wait-production",
    "scripts/check-production",
    "scripts/install-production",
    "scripts/sync-production",
    "scripts/prune-production",
    "infrastructure/production/",
)
APP_COMPOSE_PATTERN = re.compile(
    r"^infrastructure/compose/apps/compose\.(?P<service>[^/]+)\.yml$"
)


class ProductionServiceChangeError(RuntimeError):
    pass


def normalized_path(value: str) -> str:
    return value.strip().replace("\\", "/").removeprefix("./")


def project_directories() -> dict[str, Path]:
    result: dict[str, Path] = {}

    for group in ("apps", "libraries", "contracts", "keycloak-extensions"):
        group_directory = REPOSITORY_ROOT / group

        if not group_directory.is_dir():
            continue

        for build_file in group_directory.glob("*/build.gradle.kts"):
            relative_directory = build_file.parent.relative_to(REPOSITORY_ROOT)
            project = ":" + ":".join(relative_directory.parts)
            result[project] = relative_directory

    return result


def project_dependencies(projects: dict[str, Path]) -> dict[str, set[str]]:
    dependencies: dict[str, set[str]] = {}

    for project, directory in projects.items():
        build_file = REPOSITORY_ROOT / directory / "build.gradle.kts"
        content = build_file.read_text(encoding="utf-8")
        dependencies[project] = {
            match.group("project")
            for match in PROJECT_DEPENDENCY_PATTERN.finditer(content)
        }

    return dependencies


def reverse_project_dependencies(
    dependencies: dict[str, set[str]],
) -> dict[str, set[str]]:
    reverse: dict[str, set[str]] = defaultdict(set)

    for project, project_dependencies in dependencies.items():
        for dependency in project_dependencies:
            reverse[dependency].add(project)

    return reverse


def transitive_dependents(
    project: str,
    reverse_dependencies: dict[str, set[str]],
) -> set[str]:
    affected = {project}
    queue: deque[str] = deque([project])

    while queue:
        current = queue.popleft()

        for dependent in reverse_dependencies.get(current, set()):
            if dependent in affected:
                continue

            affected.add(dependent)
            queue.append(dependent)

    return affected


def project_for_path(
    path: str,
    projects: dict[str, Path],
) -> str | None:
    candidates: list[tuple[int, str]] = []

    for project, directory in projects.items():
        prefix = directory.as_posix()

        if path == prefix or path.startswith(prefix + "/"):
            candidates.append((len(prefix), project))

    if not candidates:
        return None

    return max(candidates)[1]


def gradle_project_for_service(specification: dict[str, Any]) -> str | None:
    gradle_task = specification.get("gradle_task")

    if not isinstance(gradle_task, str) or ":" not in gradle_task[1:]:
        return None

    return gradle_task.rsplit(":", maxsplit=1)[0]


def service_directories(
    catalog: dict[str, dict[str, Any]],
) -> dict[str, set[str]]:
    directories: dict[str, set[str]] = defaultdict(set)

    for name, specification in catalog.items():
        dockerfile = specification.get("dockerfile")

        if not isinstance(dockerfile, str):
            continue

        parent = Path(dockerfile).parent.as_posix()

        if parent and parent != ".":
            directories[parent].add(name)

    return directories


def add_reason(
    reasons: dict[str, set[str]],
    service: str,
    reason: str,
) -> None:
    reasons[service].add(reason)


def resolve_changes(paths: list[str]) -> dict[str, Any]:
    catalog = catalog_services()
    projects = project_directories()
    dependencies = project_dependencies(projects)
    reverse_dependencies = reverse_project_dependencies(dependencies)

    gradle_service_owners: dict[str, set[str]] = defaultdict(set)

    for name, specification in catalog.items():
        project = gradle_project_for_service(specification)

        if project is not None:
            gradle_service_owners[project].add(name)

    image_services: set[str] = set()
    runtime_services: set[str] = set()
    reasons: dict[str, set[str]] = defaultdict(set)
    manual_review_paths: set[str] = set()
    requires_full_runtime_reconcile = False

    all_gradle_services = {
        service_name
        for owners in gradle_service_owners.values()
        for service_name in owners
    }
    all_image_services = set(catalog)
    dockerfile_directories = service_directories(catalog)

    for raw_path in paths:
        path = normalized_path(raw_path)

        if not path:
            continue

        classified = False

        if path == ".dockerignore":
            classified = True

            for service_name in all_image_services:
                image_services.add(service_name)
                add_reason(reasons, service_name, ".dockerignore affects image context")

        if path in GLOBAL_GRADLE_PATHS or path.startswith(GLOBAL_GRADLE_PREFIXES):
            classified = True

            for service_name in all_gradle_services:
                image_services.add(service_name)
                add_reason(reasons, service_name, f"global Gradle input changed: {path}")

        changed_project = project_for_path(
            path=path,
            projects=projects,
        )

        if changed_project is not None:
            classified = True
            affected_projects = transitive_dependents(
                project=changed_project,
                reverse_dependencies=reverse_dependencies,
            )

            for affected_project in affected_projects:
                for service_name in gradle_service_owners.get(
                    affected_project,
                    set(),
                ):
                    image_services.add(service_name)
                    add_reason(
                        reasons,
                        service_name,
                        f"{changed_project} affects {affected_project}",
                    )

        for directory, directory_services in dockerfile_directories.items():
            if path == directory or path.startswith(directory + "/"):
                classified = True

                for service_name in directory_services:
                    image_services.add(service_name)
                    add_reason(
                        reasons,
                        service_name,
                        f"image source directory changed: {directory}",
                    )

        compose_match = APP_COMPOSE_PATTERN.fullmatch(path)

        if compose_match is not None:
            classified = True
            service_name = compose_match.group("service")

            if service_name in catalog:
                runtime_services.add(service_name)
                add_reason(
                    reasons,
                    service_name,
                    f"runtime Compose configuration changed: {path}",
                )
            else:
                manual_review_paths.add(path)

        if (
            path in FULL_RUNTIME_RECONCILE_PATHS
            or path.startswith(FULL_RUNTIME_RECONCILE_PREFIXES)
        ):
            classified = True
            requires_full_runtime_reconcile = True

        if not classified and path.startswith(DEPLOYMENT_SENSITIVE_PREFIXES):
            manual_review_paths.add(path)

    return {
        "image_services": sorted(image_services),
        "runtime_services": sorted(runtime_services),
        "requires_full_runtime_reconcile": requires_full_runtime_reconcile,
        "manual_review_paths": sorted(manual_review_paths),
        "reasons": {
            service_name: sorted(service_reasons)
            for service_name, service_reasons in sorted(reasons.items())
        },
    }


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Resolve Mnemos production services affected by changed repository paths.",
    )
    parser.add_argument(
        "--path",
        dest="paths",
        action="append",
        default=[],
        help="Changed repository path. May be provided multiple times.",
    )
    parser.add_argument(
        "--paths-file",
        type=Path,
        help="File containing one changed repository path per line.",
    )
    parser.add_argument(
        "--format",
        choices=("json", "image-names", "runtime-names"),
        default="json",
    )

    return parser.parse_args()


def read_paths(arguments: argparse.Namespace) -> list[str]:
    paths = list(arguments.paths)

    if arguments.paths_file is not None:
        paths.extend(
            arguments.paths_file.read_text(encoding="utf-8").splitlines()
        )

    if not paths and not sys.stdin.isatty():
        paths.extend(sys.stdin.read().splitlines())

    return [
        normalized_path(path)
        for path in paths
        if normalized_path(path)
    ]


def main() -> int:
    arguments = parse_arguments()

    try:
        result = resolve_changes(read_paths(arguments))
    except (
        OSError,
        ProductionServiceChangeError,
    ) as exception:
        print(f"Production service change resolution failed: {exception}", file=sys.stderr)
        return 1

    if arguments.format == "image-names":
        for service_name in result["image_services"]:
            print(service_name)
    elif arguments.format == "runtime-names":
        for service_name in result["runtime_services"]:
            print(service_name)
    else:
        print(json.dumps(result, indent=2, sort_keys=False))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
