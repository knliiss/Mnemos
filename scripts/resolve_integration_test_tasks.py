#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
PROJECT_GROUPS = ("apps", "libraries")
INTEGRATION_TAG = '@Tag("integration")'


def integration_projects() -> list[str]:
    projects: list[str] = []

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
            projects.append(":" + ":".join(relative.parts))

    return projects


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--format", choices=("json", "tasks"), default="json")
    arguments = parser.parse_args()

    projects = integration_projects()
    tasks = [f"{project}:integrationTest" for project in projects]

    if arguments.format == "tasks":
        print(" ".join(tasks))
    else:
        print(json.dumps({"projects": projects, "tasks": tasks}, indent=2))

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
