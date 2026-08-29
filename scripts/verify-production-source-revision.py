#!/usr/bin/env python3

from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

from production_source_revision import ProductionSourceRevisionError
from production_source_revision import require_trusted_source_revision


def git(repository: Path, *arguments: str) -> str:
    result = subprocess.run(
        ["git", *arguments],
        cwd=repository,
        check=True,
        capture_output=True,
        text=True,
    )

    return result.stdout.strip()


def commit_file(repository: Path, name: str, content: str, message: str) -> str:
    (repository / name).write_text(content, encoding="utf-8")
    git(repository, "add", name)
    git(repository, "commit", "-m", message)

    return git(repository, "rev-parse", "HEAD")


def expect_rejected(
    repository: Path,
    source_sha: str,
    trusted_ref: str,
) -> None:
    try:
        require_trusted_source_revision(
            source_sha=source_sha,
            trusted_ref=trusted_ref,
            repository_root=repository,
        )
    except ProductionSourceRevisionError:
        return

    raise AssertionError(f"Untrusted production source revision was accepted: {source_sha}")


def main() -> int:
    with tempfile.TemporaryDirectory(prefix="mnemos-source-revision-") as directory:
        repository = Path(directory)

        git(repository, "init", "--initial-branch=master")
        git(repository, "config", "user.email", "ci@mnemos.invalid")
        git(repository, "config", "user.name", "Mnemos CI")

        base_sha = commit_file(
            repository=repository,
            name="base.txt",
            content="base\n",
            message="base",
        )
        master_sha = commit_file(
            repository=repository,
            name="master.txt",
            content="master\n",
            message="master",
        )

        require_trusted_source_revision(
            source_sha=base_sha,
            trusted_ref="master",
            repository_root=repository,
        )
        require_trusted_source_revision(
            source_sha=master_sha,
            trusted_ref="master",
            repository_root=repository,
        )

        git(repository, "checkout", "-b", "unmerged", base_sha)
        branch_sha = commit_file(
            repository=repository,
            name="branch.txt",
            content="branch\n",
            message="branch",
        )

        expect_rejected(
            repository=repository,
            source_sha=branch_sha,
            trusted_ref="master",
        )
        expect_rejected(
            repository=repository,
            source_sha="0" * 40,
            trusted_ref="master",
        )

        try:
            require_trusted_source_revision(
                source_sha="not-a-sha",
                trusted_ref="master",
                repository_root=repository,
            )
        except ProductionSourceRevisionError:
            pass
        else:
            raise AssertionError("Malformed source SHA was accepted")

    print("Production source revision trust boundary verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
