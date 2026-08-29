#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import defaultdict, deque
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
PROJECT_GROUPS = (
    "apps",
    "contracts",
    "keycloak-extensions",
    "libraries",
)
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


class BackendCiChangeError(RuntimeError):
    pass


def normalized_path(value: str) -> str:
    return value.strip().replace("\\", "/").removeprefix("./")


def project_directories() -> dict[str, Path]:
    projects: dict[str, Path] = {}

    for group in PROJECT_GROUPS:
        group_directory = REPOSITORY_ROOT / group

        if not group_directory.is_dir():
            continue

        for build_file in group_directory.glob("*/build.gradle.kts"):
            relative_directory = build_file.parent.relative_to(REPOSITORY_ROOT)
            project = ":" + ":".join(relative_directory.parts)
            projects[project] = relative_directory

    return projects


def project_dependencies(projects: dict[str, Path]) -> dict[str, set[str]]:
    dependencies: dict[str, set[str]] = {}

    for project, directory in projects.items():
        build_file = REPOSITORY_ROOT / directory / "build.gradle.kts"
        content = build_file.read_text(encoding="utf-8")
        dependencies[project] = {
            match.group("project")
            for match in PROJECT_DEPENDENCY_PATTERN.finditer(content)
            if match.group("project") in projects
        }

    return dependencies


def reverse_dependencies(
    dependencies: dict[str, set[str]],
) -> dict[str, set[str]]:
    reverse: dict[str, set[str]] = defaultdict(set)

    for project, project_dependencies in dependencies.items():
        for dependency in project_dependencies:
            reverse[dependency].add(project)

    return reverse


def transitive_dependents(
    project: str,
    reverse: dict[str, set[str]],
) -> set[str]:
    affected = {project}
    pending: deque[str] = deque([project])

    while pending:
        current = pending.popleft()

        for dependent in reverse.get(current, set()):
            if dependent in affected:
                continue

            affected.add(dependent)
            pending.append(dependent)

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


def resolve_changes(paths: list[str]) -> dict[str, object]:
    projects = project_directories()
    dependencies = project_dependencies(projects)
    reverse = reverse_dependencies(dependencies)

    affected_projects: set[str] = set()
    dependency_scan_required = False
    full_backend = False

    for raw_path in paths:
        path = normalized_path(raw_path)

        if not path:
            continue

        if path in GLOBAL_GRADLE_PATHS or path.startswith(GLOBAL_GRADLE_PREFIXES):
            full_backend = True
            dependency_scan_required = True
            continue

        changed_project = project_for_path(
            path=path,
            projects=projects,
        )

        if changed_project is None:
            continue

        affected_projects.update(
            transitive_dependents(
                project=changed_project,
                reverse=reverse,
            )
        )

        if path.endswith("/build.gradle.kts"):
            dependency_scan_required = True

    if full_backend:
        affected_projects = set(projects)

    check_tasks = [
        f"{project}:check"
        for project in sorted(affected_projects)
    ]

    return {
        "affected_projects": sorted(affected_projects),
        "check_tasks": check_tasks,
        "dependency_scan_required": dependency_scan_required,
        "full_backend": full_backend,
    }


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Resolve Gradle projects affected by repository changes for backend CI.",
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
        choices=("json", "check-tasks"),
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
    except (OSError, BackendCiChangeError) as exception:
        print(f"Backend CI change resolution failed: {exception}", file=sys.stderr)
        return 1

    if arguments.format == "check-tasks":
        for task in result["check_tasks"]:
            print(task)
    else:
        print(json.dumps(result, indent=2, sort_keys=False))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
