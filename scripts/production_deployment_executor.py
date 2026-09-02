#!/usr/bin/env python3

from __future__ import annotations

import subprocess
from pathlib import Path
from typing import Any

import production_deployment_executor_base as base


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
AVAILABILITY_SCRIPT = REPOSITORY_ROOT / "scripts" / "verify-production-availability.sh"


ProductionDeploymentExecutionError = base.ProductionDeploymentExecutionError
execution_record = base.execution_record
persist_execution_record = base.persist_execution_record
run_steps = base.run_steps


def availability_steps() -> list[dict[str, Any]]:
    return [
        {
            "kind": "command",
            "command": ["sh", str(AVAILABILITY_SCRIPT)],
        }
    ]


def rollback_attempted_actions(
    attempted: list[dict[str, Any]],
) -> list[str]:
    errors: list[str] = []

    for action in reversed(attempted):
        service = action.get("service", "unknown")

        try:
            run_steps(base.rollback_steps(action))
            run_steps(availability_steps())
        except (OSError, subprocess.CalledProcessError) as exception:
            errors.append(f"{service}: {exception}")

    return errors


def execute_transition(
    current_state: dict[str, Any],
    target_state: dict[str, Any],
    transition: dict[str, Any],
    state_dir: Path,
) -> None:
    actions = base.validate_scoped_transition(
        current_state=current_state,
        target_state=target_state,
        transition=transition,
    )
    started_at = base.now_iso()
    attempted: list[dict[str, Any]] = []

    base.persist_state_snapshot(state_dir, current_state)

    try:
        for action in actions:
            run_steps(base.preflight_steps(action))

        run_steps(availability_steps())
    except (OSError, subprocess.CalledProcessError) as exception:
        base.persist_failed_execution(
            state_dir=state_dir,
            transition=transition,
            status="preflight-failed",
            started_at=started_at,
            attempted=attempted,
            error=exception,
        )
        raise base.ProductionDeploymentExecutionError(
            "Production runtime or availability does not match the recorded "
            f"deployment baseline: {exception}"
        ) from exception

    try:
        for action in actions:
            attempted.append(action)
            run_steps(base.target_steps(action))
            run_steps(availability_steps())
    except (OSError, subprocess.CalledProcessError) as exception:
        rollback_errors = rollback_attempted_actions(attempted)
        rollback_error = "; ".join(rollback_errors) or None
        status = "rollback-failed" if rollback_error is not None else "rolled-back"

        base.persist_failed_execution(
            state_dir=state_dir,
            transition=transition,
            status=status,
            started_at=started_at,
            attempted=attempted,
            error=exception,
            rollback_error=rollback_error,
        )

        if rollback_error is not None:
            raise base.ProductionDeploymentExecutionError(
                f"Deployment failed and rollback failed: {exception}; {rollback_error}"
            ) from exception

        raise base.ProductionDeploymentExecutionError(
            f"Deployment failed and was rolled back: {exception}"
        ) from exception

    base.persist_state_snapshot(state_dir, target_state)

    record = execution_record(
        transition=transition,
        status="applied",
        started_at=started_at,
        completed_at=base.now_iso(),
        attempted_services=[action["service"] for action in attempted],
    )
    persist_execution_record(
        state_dir=state_dir,
        transition=transition,
        record=record,
    )
    base.atomic_write_json(state_dir / "current-state.json", target_state)


def dry_run_bundle(
    current_state: dict[str, Any],
    target_state: dict[str, Any],
    transition: dict[str, Any],
) -> dict[str, Any]:
    bundle = base.dry_run_bundle(
        current_state=current_state,
        target_state=target_state,
        transition=transition,
    )
    bundle["availability_before"] = availability_steps()

    for action in bundle["actions"]:
        action["availability_after"] = availability_steps()

    return bundle


def __getattr__(name: str) -> Any:
    return getattr(base, name)


def main() -> int:
    base.execute_transition = execute_transition
    base.dry_run_bundle = dry_run_bundle
    return base.main()


if __name__ == "__main__":
    raise SystemExit(main())
