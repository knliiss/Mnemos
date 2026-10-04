#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from resolve_backend_ci_changes import resolve_changes


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
PROJECT_GROUPS = ("apps", "libraries")
INTEGRATION_TAG = '@Tag("integration")'
FORCE_ALL_PATHS = {
    ".github/workflows/integration-verify.yml",
    "scripts/resolve_integration_test_tasks.py",
}


def integration_projects() -> set[str]:
    projects: set[str] = set()

    for group in PROJECT_GROUPS:
        group_directory = REPOSITORY_ROOT / group

        if not group_directory.is_dir():
            continue

        for build_file in sorted(group_directory.glob("*/build.gradle.kts")):
            project_directory = build_file.parent
            test_directory = project_directory / "src" / "test"

            if not test_directory.is_dir():
                continue

            has_integration_test = any(
                INTEGRATION_TAG in source.read_text(encoding="utf-8")
                for pattern in ("*.kt", "*.java")
                for source in test_directory.rglob(pattern)
            )

            if not has_integration_test:
                continue

            relative = project_directory.relative_to(REPOSITORY_ROOT)
            projects.add(":" + ":".join(relative.parts))

    return projects


def read_paths(arguments: argparse.Namespace) -> list[str]:
    paths = list(arguments.path)

    if arguments.paths_file is not None:
        paths.extend(
            arguments.paths_file.read_text(encoding="utf-8").splitlines()
        )

    if not paths and not sys.stdin.isatty():
        paths.extend(sys.stdin.read().splitlines())

    return [
        path.strip().replace("\\", "/").removeprefix("./")
        for path in paths
        if path.strip()
    ]


def resolve_tasks(
    paths: list[str],
    force_all: bool,
) -> dict[str, list[str]]:
    available = integration_projects()

    if force_all or any(path in FORCE_ALL_PATHS for path in paths):
        selected = available
    else:
        affected = set(resolve_changes(paths)["affected_projects"])
        selected = available & affected

    projects = sorted(selected)

    return {
        "projects": projects,
        "tasks": [
            f"{project}:integrationTest"
            for project in projects
        ],
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--path", action="append", default=[])
    parser.add_argument("--paths-file", type=Path)
    parser.add_argument("--all", action="store_true")
    parser.add_argument("--format", choices=("json", "tasks"), default="json")
    arguments = parser.parse_args()

    result = resolve_tasks(
        paths=read_paths(arguments),
        force_all=arguments.all,
    )

    if arguments.format == "tasks":
        print(" ".join(result["tasks"]))
    else:
        print(json.dumps(result, indent=2))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
