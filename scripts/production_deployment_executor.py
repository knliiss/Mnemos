#!/usr/bin/env python3

from __future__ import annotations

import argparse
import fcntl
import json
import os
import subprocess
import sys
import tempfile
from datetime import UTC
from datetime import datetime
from pathlib import Path
from typing import Any

from production_deployment_plan import ProductionDeploymentPlanError
from production_deployment_state import ProductionDeploymentStateError
from production_deployment_state import read_state
from production_deployment_state import read_transition
from production_deployment_state import validate_state


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_STATE_DIR = Path(
    os.environ.get("MNEMOS_DEPLOY_STATE_DIR", "/var/lib/mnemos-deploy")
)
DEFAULT_TIMEOUT_SECONDS = 300


class ProductionDeploymentExecutionError(RuntimeError):
    pass


def require_execution_gate() -> None:
    if os.environ.get("MNEMOS_PRODUCTION_DEPLOY_ENABLED") != "true":
        raise ProductionDeploymentExecutionError(
            "MNEMOS_PRODUCTION_DEPLOY_ENABLED must be true"
        )

    if os.environ.get("MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED") != "true":
        raise ProductionDeploymentExecutionError(
            "MNEMOS_PRODUCTION_SCOPED_DEPLOY_ENABLED must be true"
        )


def require_root() -> None:
    if os.geteuid() != 0:
        raise ProductionDeploymentExecutionError(
            "Scoped production deployment must run as root"
        )


def compose_registry(image: str, service: str, source_sha: str) -> str:
    expected_suffix = f"/mnemos-{service}:{source_sha}"

    if not image.endswith(expected_suffix):
        raise ProductionDeploymentExecutionError(
            f"Cannot derive registry from image for {service}: {image}"
        )

    registry = image[: -len(expected_suffix)]

    if not registry:
        raise ProductionDeploymentExecutionError(
            f"Image registry is empty for {service}: {image}"
        )

    return registry


def stable_seconds(action: dict[str, Any]) -> int:
    verification = action.get("verification")

    if not isinstance(verification, dict):
        raise ProductionDeploymentExecutionError(
            f"Deployment action has no verification policy: {action.get('service')}"
        )

    value = verification.get("stable_seconds")

    if not isinstance(value, int) or value <= 0:
        raise ProductionDeploymentExecutionError(
            f"Deployment action has invalid stable_seconds: {action.get('service')}"
        )

    return value


def swarm_steps(
    action: dict[str, Any],
    image: str,
) -> list[dict[str, Any]]:
    runtime = action["runtime"]["name"]

    return [
        {
            "kind": "command",
            "command": [
                "docker",
                "service",
                "update",
                "--detach=true",
                "--with-registry-auth",
                "--image",
                image,
                runtime,
            ],
        },
        {
            "kind": "command",
            "command": [
                "sh",
                str(REPOSITORY_ROOT / "scripts" / "wait-production-service.sh"),
                runtime,
                image,
                str(DEFAULT_TIMEOUT_SECONDS),
                str(stable_seconds(action)),
            ],
        },
    ]


def compose_steps(
    action: dict[str, Any],
    image: str,
    source_sha: str,
) -> list[dict[str, Any]]:
    service = action["service"]
    runtime = action["runtime"]["name"]
    registry = compose_registry(
        image=image,
        service=service,
        source_sha=source_sha,
    )
    compose_environment = {
        "MNEMOS_IMAGE_REGISTRY": registry,
        "MNEMOS_RELEASE_TAG": source_sha,
    }

    return [
        {
            "kind": "command",
            "command": ["docker", "pull", image],
        },
        {
            "kind": "command",
            "command": [
                "sh",
                str(REPOSITORY_ROOT / "scripts" / "production-infra.sh"),
                "up",
                "-d",
                "--no-deps",
                service,
            ],
            "environment": compose_environment,
        },
        {
            "kind": "command",
            "command": [
                "sh",
                str(REPOSITORY_ROOT / "scripts" / "wait-production-container.sh"),
                runtime,
                image,
                str(DEFAULT_TIMEOUT_SECONDS),
                str(stable_seconds(action)),
            ],
        },
    ]


def action_steps(
    action: dict[str, Any],
    image: str,
    source_sha: str,
) -> list[dict[str, Any]]:
    runtime = action.get("runtime")

    if not isinstance(runtime, dict):
        raise ProductionDeploymentExecutionError(
            f"Deployment action has no runtime: {action.get('service')}"
        )

    runtime_type = runtime.get("type")

    if runtime_type == "swarm":
        return swarm_steps(
            action=action,
            image=image,
        )

    if runtime_type == "compose":
        return compose_steps(
            action=action,
            image=image,
            source_sha=source_sha,
        )

    raise ProductionDeploymentExecutionError(
        f"Unsupported scoped deployment runtime: {runtime_type}"
    )


def target_steps(action: dict[str, Any]) -> list[dict[str, Any]]:
    target = action["to"]

    return action_steps(
        action=action,
        image=target["image"],
        source_sha=target["source_sha"],
    )


def rollback_steps(action: dict[str, Any]) -> list[dict[str, Any]]:
    rollback = action["rollback"]

    return action_steps(
        action=action,
        image=rollback["image"],
        source_sha=rollback["source_sha"],
    )


def run_step(step: dict[str, Any]) -> None:
    command = step.get("command")

    if not isinstance(command, list) or not all(
        isinstance(value, str) and value for value in command
    ):
        raise ProductionDeploymentExecutionError(
            f"Invalid execution command: {command}"
        )

    environment = os.environ.copy()
    extra_environment = step.get("environment", {})

    if not isinstance(extra_environment, dict):
        raise ProductionDeploymentExecutionError(
            "Execution step environment must be an object"
        )

    environment.update(
        {
            str(key): str(value)
            for key, value in extra_environment.items()
        }
    )

    subprocess.run(
        command,
        cwd=REPOSITORY_ROOT,
        env=environment,
        check=True,
    )


def run_steps(steps: list[dict[str, Any]]) -> None:
    for step in steps:
        run_step(step)


def validate_scoped_transition(
    current_state: dict[str, Any],
    target_state: dict[str, Any],
    transition: dict[str, Any],
) -> list[dict[str, Any]]:
    plan = transition["plan"]

    if plan["mode"] != "scoped":
        raise ProductionDeploymentExecutionError(
            "Scoped executor refuses full runtime reconcile plans"
        )

    if current_state["tooling_revision"] != target_state["tooling_revision"]:
        raise ProductionDeploymentExecutionError(
            "Scoped service deployment cannot change the production tooling revision"
        )

    actions = plan["actions"]

    if not actions:
        raise ProductionDeploymentExecutionError(
            "Scoped deployment plan contains no actions"
        )

    for action in actions:
        if action.get("operation") != "image-update":
            raise ProductionDeploymentExecutionError(
                f"Scoped executor only accepts image-update actions: {action.get('service')}"
            )

        target_steps(action)
        rollback_steps(action)

    return actions


def now_iso() -> str:
    return datetime.now(UTC).isoformat()


def atomic_write_json(path: Path, value: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
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


def persist_state_snapshot(state_dir: Path, state: dict[str, Any]) -> None:
    state_id = state["state_id"].removeprefix("sha256:")
    snapshot = state_dir / "states" / f"{state_id}.json"

    if snapshot.exists():
        existing = json.loads(snapshot.read_text(encoding="utf-8"))
        validate_state(existing)

        if existing != state:
            raise ProductionDeploymentExecutionError(
                f"Deployment state snapshot collision: {state['state_id']}"
            )

        return

    atomic_write_json(snapshot, state)


def execution_record(
    transition: dict[str, Any],
    status: str,
    started_at: str,
    completed_at: str,
    error: str | None = None,
    rollback_error: str | None = None,
) -> dict[str, Any]:
    execution: dict[str, Any] = {
        "status": status,
        "started_at": started_at,
        "completed_at": completed_at,
    }

    if error is not None:
        execution["error"] = error

    if rollback_error is not None:
        execution["rollback_error"] = rollback_error

    return {
        "schema_version": 1,
        "transition": transition,
        "execution": execution,
    }


def persist_execution_record(
    state_dir: Path,
    transition: dict[str, Any],
    record: dict[str, Any],
) -> None:
    transition_id = transition["transition_id"].removeprefix("sha256:")
    path = state_dir / "transitions" / f"{transition_id}.json"
    atomic_write_json(path, record)


def execute_transition(
    current_state: dict[str, Any],
    target_state: dict[str, Any],
    transition: dict[str, Any],
    state_dir: Path,
) -> None:
    actions = validate_scoped_transition(
        current_state=current_state,
        target_state=target_state,
        transition=transition,
    )
    started_at = now_iso()
    attempted: list[dict[str, Any]] = []

    persist_state_snapshot(state_dir, current_state)

    try:
        for action in actions:
            attempted.append(action)
            run_steps(target_steps(action))
    except (OSError, subprocess.CalledProcessError) as exception:
        rollback_error: str | None = None

        try:
            for action in reversed(attempted):
                run_steps(rollback_steps(action))
        except (OSError, subprocess.CalledProcessError) as rollback_exception:
            rollback_error = str(rollback_exception)

        status = "rollback-failed" if rollback_error is not None else "rolled-back"
        record = execution_record(
            transition=transition,
            status=status,
            started_at=started_at,
            completed_at=now_iso(),
            error=str(exception),
            rollback_error=rollback_error,
        )
        persist_execution_record(
            state_dir=state_dir,
            transition=transition,
            record=record,
        )

        if rollback_error is not None:
            raise ProductionDeploymentExecutionError(
                f"Deployment failed and rollback failed: {exception}; {rollback_error}"
            ) from exception

        raise ProductionDeploymentExecutionError(
            f"Deployment failed and was rolled back: {exception}"
        ) from exception

    persist_state_snapshot(state_dir, target_state)
    atomic_write_json(state_dir / "current-state.json", target_state)

    record = execution_record(
        transition=transition,
        status="applied",
        started_at=started_at,
        completed_at=now_iso(),
    )
    persist_execution_record(
        state_dir=state_dir,
        transition=transition,
        record=record,
    )


def dry_run_bundle(
    current_state: dict[str, Any],
    target_state: dict[str, Any],
    transition: dict[str, Any],
) -> dict[str, Any]:
    actions = validate_scoped_transition(
        current_state=current_state,
        target_state=target_state,
        transition=transition,
    )

    return {
        "transition_id": transition["transition_id"],
        "action_count": len(actions),
        "actions": [
            {
                "service": action["service"],
                "apply": target_steps(action),
                "rollback": rollback_steps(action),
            }
            for action in actions
        ],
    }


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Execute a validated scoped Mnemos production deployment transition. "
            "Without --execute this command only prints the exact apply/rollback steps."
        ),
    )
    parser.add_argument("--transition", type=Path, required=True)
    parser.add_argument("--target-state", type=Path, required=True)
    parser.add_argument("--state-dir", type=Path, default=DEFAULT_STATE_DIR)
    parser.add_argument("--expected-transition-id")
    parser.add_argument("--execute", action="store_true")

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        current_state_path = arguments.state_dir / "current-state.json"
        current_state = read_state(current_state_path)
        target_state = read_state(arguments.target_state)
        transition = read_transition(
            path=arguments.transition,
            current=current_state,
            target=target_state,
        )

        if not arguments.execute:
            json.dump(
                dry_run_bundle(
                    current_state=current_state,
                    target_state=target_state,
                    transition=transition,
                ),
                sys.stdout,
                indent=2,
            )
            sys.stdout.write("\n")
            return 0

        require_root()
        require_execution_gate()

        if arguments.expected_transition_id != transition["transition_id"]:
            raise ProductionDeploymentExecutionError(
                "--expected-transition-id must exactly match the validated transition"
            )

        arguments.state_dir.mkdir(parents=True, exist_ok=True)
        os.chmod(arguments.state_dir, 0o700)
        lock_path = arguments.state_dir / ".scoped-deploy.lock"

        with lock_path.open("a+", encoding="utf-8") as lock_file:
            try:
                fcntl.flock(
                    lock_file.fileno(),
                    fcntl.LOCK_EX | fcntl.LOCK_NB,
                )
            except BlockingIOError as exception:
                raise ProductionDeploymentExecutionError(
                    "Another scoped production deployment is already running"
                ) from exception

            execute_transition(
                current_state=current_state,
                target_state=target_state,
                transition=transition,
                state_dir=arguments.state_dir,
            )

        print(
            "Scoped production deployment applied: "
            f"{transition['transition_id']}"
        )
        return 0
    except (
        json.JSONDecodeError,
        OSError,
        ProductionDeploymentExecutionError,
        ProductionDeploymentPlanError,
        ProductionDeploymentStateError,
    ) as exception:
        print(f"Scoped production deployment failed: {exception}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
