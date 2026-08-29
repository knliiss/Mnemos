#!/usr/bin/env python3

from __future__ import annotations

import argparse
import fcntl
import json
import os
import sys
import tempfile
from contextlib import contextmanager
from pathlib import Path
from typing import Any
from typing import Iterator

from production_deployment_state import ProductionDeploymentStateError
from production_deployment_state import read_state
from production_deployment_state import validate_state


DEFAULT_STATE_DIR = Path(
    os.environ.get("MNEMOS_DEPLOY_STATE_DIR", "/var/lib/mnemos-deploy")
)


class ProductionDeploymentStoreError(RuntimeError):
    pass


def ensure_private_directory(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)
    os.chmod(path, 0o700)


def atomic_write_json(path: Path, value: dict[str, Any]) -> None:
    ensure_private_directory(path.parent)
    serialized = json.dumps(value, indent=2, sort_keys=False) + "\n"

    with tempfile.NamedTemporaryFile(
        mode="w",
        encoding="utf-8",
        dir=path.parent,
        prefix=f".{path.name}.",
        delete=False,
    ) as temporary:
        temporary.write(serialized)
        temporary.flush()
        os.fsync(temporary.fileno())
        temporary_path = Path(temporary.name)

    os.chmod(temporary_path, 0o600)
    os.replace(temporary_path, path)


def create_json_exclusive(path: Path, value: dict[str, Any]) -> None:
    ensure_private_directory(path.parent)
    serialized = json.dumps(value, indent=2, sort_keys=False) + "\n"

    try:
        descriptor = os.open(
            path,
            os.O_WRONLY | os.O_CREAT | os.O_EXCL,
            0o600,
        )
    except FileExistsError as exception:
        raise ProductionDeploymentStoreError(
            f"Deployment state file already exists: {path}"
        ) from exception

    try:
        with os.fdopen(descriptor, "w", encoding="utf-8") as file:
            file.write(serialized)
            file.flush()
            os.fsync(file.fileno())
    except Exception:
        path.unlink(missing_ok=True)
        raise


def state_snapshot_path(state_dir: Path, state_id: str) -> Path:
    if not state_id.startswith("sha256:"):
        raise ProductionDeploymentStoreError(
            f"Deployment state ID is invalid: {state_id}"
        )

    digest = state_id.removeprefix("sha256:")

    if len(digest) != 64 or any(character not in "0123456789abcdef" for character in digest):
        raise ProductionDeploymentStoreError(
            f"Deployment state ID is invalid: {state_id}"
        )

    return state_dir / "states" / f"{digest}.json"


def persist_state_snapshot(state_dir: Path, state: dict[str, Any]) -> Path:
    validate_state(state)
    snapshot = state_snapshot_path(
        state_dir=state_dir,
        state_id=state["state_id"],
    )

    if snapshot.exists():
        existing = read_state(snapshot)

        if existing != state:
            raise ProductionDeploymentStoreError(
                f"Deployment state snapshot collision: {state['state_id']}"
            )

        return snapshot

    create_json_exclusive(snapshot, state)
    return snapshot


def current_state_path(state_dir: Path) -> Path:
    return state_dir / "current-state.json"


def read_current_state(state_dir: Path) -> dict[str, Any]:
    return read_state(current_state_path(state_dir))


def require_expected_state_id(
    state: dict[str, Any],
    expected_state_id: str,
) -> None:
    if expected_state_id != state["state_id"]:
        raise ProductionDeploymentStoreError(
            "Expected deployment state ID does not match the validated state: "
            f"expected={expected_state_id} actual={state['state_id']}"
        )


def initialize_current_state(
    state_dir: Path,
    state: dict[str, Any],
    expected_state_id: str,
) -> str:
    validate_state(state)
    require_expected_state_id(
        state=state,
        expected_state_id=expected_state_id,
    )
    ensure_private_directory(state_dir)

    current_path = current_state_path(state_dir)

    if current_path.exists():
        current = read_state(current_path)

        if current == state:
            persist_state_snapshot(state_dir, state)
            return "already-initialized"

        raise ProductionDeploymentStoreError(
            "Production deployment state is already initialized with a different state"
        )

    persist_state_snapshot(state_dir, state)
    create_json_exclusive(current_path, state)
    return "initialized"


def replace_current_state(
    state_dir: Path,
    state: dict[str, Any],
    expected_state_id: str,
) -> None:
    validate_state(state)
    require_expected_state_id(
        state=state,
        expected_state_id=expected_state_id,
    )
    ensure_private_directory(state_dir)
    persist_state_snapshot(state_dir, state)
    atomic_write_json(current_state_path(state_dir), state)


@contextmanager
def deployment_lock(state_dir: Path) -> Iterator[None]:
    ensure_private_directory(state_dir)
    lock_path = state_dir / ".scoped-deploy.lock"

    with lock_path.open("a+", encoding="utf-8") as lock_file:
        os.chmod(lock_path, 0o600)

        try:
            fcntl.flock(
                lock_file.fileno(),
                fcntl.LOCK_EX | fcntl.LOCK_NB,
            )
        except BlockingIOError as exception:
            raise ProductionDeploymentStoreError(
                "Another production deployment state transition is already running"
            ) from exception

        try:
            yield
        finally:
            fcntl.flock(lock_file.fileno(), fcntl.LOCK_UN)


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Initialize, replace, or validate the durable Mnemos production deployment state."
        ),
    )
    parser.add_argument(
        "--state-dir",
        type=Path,
        default=DEFAULT_STATE_DIR,
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    subparsers.add_parser("validate-current")

    initialize_parser = subparsers.add_parser("initialize")
    initialize_parser.add_argument("--input", type=Path, required=True)
    initialize_parser.add_argument("--expected-state-id", required=True)

    replace_parser = subparsers.add_parser("replace")
    replace_parser.add_argument("--input", type=Path, required=True)
    replace_parser.add_argument("--expected-state-id", required=True)

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "validate-current":
            state = read_current_state(arguments.state_dir)
            print(f"Production deployment state verified: {state['state_id']}")
            return 0

        state = read_state(arguments.input)

        with deployment_lock(arguments.state_dir):
            if arguments.command == "initialize":
                result = initialize_current_state(
                    state_dir=arguments.state_dir,
                    state=state,
                    expected_state_id=arguments.expected_state_id,
                )
                print(
                    "Production deployment state initialization: "
                    f"{result} {state['state_id']}"
                )
                return 0

            if arguments.command == "replace":
                replace_current_state(
                    state_dir=arguments.state_dir,
                    state=state,
                    expected_state_id=arguments.expected_state_id,
                )
                print(f"Production deployment state replaced: {state['state_id']}")
                return 0
    except (
        OSError,
        ProductionDeploymentStateError,
        ProductionDeploymentStoreError,
    ) as exception:
        print(f"Production deployment state store failed: {exception}", file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
