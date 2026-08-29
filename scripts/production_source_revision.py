#!/usr/bin/env python3

from __future__ import annotations

import argparse
import re
import subprocess
from pathlib import Path


SOURCE_SHA_PATTERN = re.compile(r"^[0-9a-f]{40}$")
REPOSITORY_ROOT = Path(__file__).resolve().parent.parent


class ProductionSourceRevisionError(RuntimeError):
    pass


def require_trusted_source_revision(
    source_sha: str,
    trusted_ref: str = "HEAD",
    repository_root: Path = REPOSITORY_ROOT,
) -> None:
    if not SOURCE_SHA_PATTERN.fullmatch(source_sha):
        raise ProductionSourceRevisionError(
            "Production source revision must be a full lowercase 40-character Git SHA"
        )

    if not trusted_ref.strip():
        raise ProductionSourceRevisionError("Trusted Git ref must not be blank")

    try:
        subprocess.run(
            ["git", "cat-file", "-e", f"{source_sha}^{{commit}}"],
            cwd=repository_root,
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    except subprocess.CalledProcessError as exception:
        raise ProductionSourceRevisionError(
            f"Production source revision does not exist: {source_sha}"
        ) from exception

    try:
        trusted_sha = subprocess.run(
            ["git", "rev-parse", "--verify", f"{trusted_ref}^{{commit}}"],
            cwd=repository_root,
            check=True,
            capture_output=True,
            text=True,
        ).stdout.strip()
    except subprocess.CalledProcessError as exception:
        raise ProductionSourceRevisionError(
            f"Trusted production ref does not resolve to a commit: {trusted_ref}"
        ) from exception

    try:
        subprocess.run(
            ["git", "merge-base", "--is-ancestor", source_sha, trusted_sha],
            cwd=repository_root,
            check=True,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    except subprocess.CalledProcessError as exception:
        raise ProductionSourceRevisionError(
            "Production source revision is not reachable from the trusted production "
            f"history: {source_sha}"
        ) from exception


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Validate that a production source revision belongs to trusted Git history."
    )
    parser.add_argument("source_sha", help="Full lowercase 40-character Git commit SHA")
    parser.add_argument(
        "--trusted-ref",
        default="HEAD",
        help="Git ref whose history is trusted for production sources",
    )

    return parser.parse_args()


def main() -> None:
    arguments = parse_arguments()

    require_trusted_source_revision(
        source_sha=arguments.source_sha,
        trusted_ref=arguments.trusted_ref,
    )


if __name__ == "__main__":
    main()
