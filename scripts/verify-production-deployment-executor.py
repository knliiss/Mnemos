#!/usr/bin/env python3

from __future__ import annotations

import sys
import tempfile
from pathlib import Path

from production_deployment_executor import ProductionDeploymentExecutionError
from production_deployment_executor import dry_run_bundle
from production_deployment_executor import execution_record
from production_deployment_executor import persist_execution_record
from production_deployment_plan import build_plan
from production_deployment_state import build_state
from production_deployment_state import build_transition
from production_release_manifest import create_manifest
from production_release_manifest import update_manifest


BASE_SHA = "1" * 40
ACCOUNT_SHA = "2" * 40
KEYCLOAK_SHA = "3" * 40
TOOLING_SHA = "4" * 40
NEXT_TOOLING_SHA = "5" * 40
REGISTRY = "ghcr.io/example"


def fail(message: str) -> None:
    raise AssertionError(message)


def transition_for(
    assignments: list[tuple[str, str]],
    tooling_revision: str = TOOLING_SHA,
    full_reconcile: bool = False,
) -> tuple[dict[str, object], dict[str, object], dict[str, object]]:
    current_manifest = create_manifest(
        registry=REGISTRY,
        source_sha=BASE_SHA,
    )
    target_manifest = update_manifest(
        base=current_manifest,
        assignments=assignments,
    )
    current_state = build_state(
        tooling_revision=TOOLING_SHA,
        release_manifest=current_manifest,
    )
    target_state = build_state(
        tooling_revision=tooling_revision,
        release_manifest=target_manifest,
    )
    plan = build_plan(
        current=current_manifest,
        target=target_manifest,
        requires_full_runtime_reconcile=full_reconcile,
        allow_full_runtime_reconcile=full_reconcile,
    )
    transition = build_transition(
        current=current_state,
        target=target_state,
        plan=plan,
        actor="github-actions",
        reason="executor verification",
    )

    return current_state, target_state, transition


def flattened_commands(bundle: dict[str, object]) -> list[list[str]]:
    commands: list[list[str]] = []

    for action in bundle["actions"]:
        for phase in ("apply", "rollback"):
            for step in action[phase]:
                command = step["command"]
                commands.append(command)

    return commands


def verify_account_service_update() -> None:
    current_state, target_state, transition = transition_for(
        assignments=[("account-service", ACCOUNT_SHA)],
    )
    bundle = dry_run_bundle(
        current_state=current_state,
        target_state=target_state,
        transition=transition,
    )

    if bundle["action_count"] != 1:
        fail(f"Account executor bundle has unexpected action count: {bundle}")

    action = bundle["actions"][0]
    apply_commands = [step["command"] for step in action["apply"]]

    expected_update = [
        "docker",
        "service",
        "update",
        "--detach=true",
        "--with-registry-auth",
        "--image",
        f"{REGISTRY}/mnemos-account-service:{ACCOUNT_SHA}",
        "mnemos_account-service",
    ]

    if apply_commands[0] != expected_update:
        fail(f"Account executor does not use targeted service update: {apply_commands}")

    if "wait-production-service.sh" not in apply_commands[1][1]:
        fail(f"Account executor does not use targeted service convergence: {apply_commands}")

    for command in flattened_commands(bundle):
        if command[:3] == ["docker", "stack", "deploy"]:
            fail("Scoped account executor unexpectedly uses docker stack deploy")


def verify_keycloak_update() -> None:
    current_state, target_state, transition = transition_for(
        assignments=[("keycloak", KEYCLOAK_SHA)],
    )
    bundle = dry_run_bundle(
        current_state=current_state,
        target_state=target_state,
        transition=transition,
    )
    action = bundle["actions"][0]
    apply_steps = action["apply"]

    if apply_steps[0]["command"] != [
        "docker",
        "pull",
        f"{REGISTRY}/mnemos-keycloak:{KEYCLOAK_SHA}",
    ]:
        fail(f"Keycloak executor does not pull only its target image: {apply_steps}")

    compose_command = apply_steps[1]["command"]

    if compose_command[-4:] != ["up", "-d", "--no-deps", "keycloak"]:
        fail(f"Keycloak executor does not use scoped Compose update: {compose_command}")

    compose_environment = apply_steps[1].get("environment")

    if compose_environment != {
        "MNEMOS_IMAGE_REGISTRY": REGISTRY,
        "MNEMOS_RELEASE_TAG": KEYCLOAK_SHA,
    }:
        fail(f"Keycloak executor uses unexpected Compose image context: {apply_steps}")

    if "wait-production-container.sh" not in apply_steps[2]["command"][1]:
        fail(f"Keycloak executor does not use targeted container health: {apply_steps}")


def verify_executor_guards() -> None:
    current_state, target_state, full_transition = transition_for(
        assignments=[("account-service", ACCOUNT_SHA)],
        full_reconcile=True,
    )

    try:
        dry_run_bundle(
            current_state=current_state,
            target_state=target_state,
            transition=full_transition,
        )
    except ProductionDeploymentExecutionError:
        pass
    else:
        fail("Scoped executor accepted a full runtime reconcile transition")

    current_state, target_state, tooling_transition = transition_for(
        assignments=[("account-service", ACCOUNT_SHA)],
        tooling_revision=NEXT_TOOLING_SHA,
    )

    try:
        dry_run_bundle(
            current_state=current_state,
            target_state=target_state,
            transition=tooling_transition,
        )
    except ProductionDeploymentExecutionError:
        return

    fail("Scoped executor accepted a tooling revision change")


def verify_append_only_execution_history() -> None:
    _, _, transition = transition_for(
        assignments=[("account-service", ACCOUNT_SHA)],
    )
    first = execution_record(
        transition=transition,
        status="rolled-back",
        started_at="2026-08-29T04:30:00+00:00",
        completed_at="2026-08-29T04:30:05+00:00",
        attempted_services=["account-service"],
        error="simulated failure",
    )
    second = execution_record(
        transition=transition,
        status="applied",
        started_at="2026-08-29T04:31:00+00:00",
        completed_at="2026-08-29T04:31:05+00:00",
        attempted_services=["account-service"],
    )

    with tempfile.TemporaryDirectory() as directory:
        state_dir = Path(directory)
        first_path = persist_execution_record(
            state_dir=state_dir,
            transition=transition,
            record=first,
        )
        second_path = persist_execution_record(
            state_dir=state_dir,
            transition=transition,
            record=second,
        )

        if first_path == second_path:
            fail("Separate deployment attempts resolved to the same audit path")

        if not first_path.is_file() or not second_path.is_file():
            fail("Deployment attempt history was not persisted append-only")

        history = list(first_path.parent.glob("*.json"))

        if len(history) != 2:
            fail(f"Expected two immutable execution records, got {history}")

        try:
            persist_execution_record(
                state_dir=state_dir,
                transition=transition,
                record=first,
            )
        except ProductionDeploymentExecutionError:
            return

    fail("Duplicate deployment attempt overwrote immutable audit history")


def main() -> int:
    verify_account_service_update()
    verify_keycloak_update()
    verify_executor_guards()
    verify_append_only_execution_history()

    print("Production scoped deployment executor behavior verified")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except AssertionError as exception:
        print(
            f"Production scoped deployment executor verification failed: {exception}",
            file=sys.stderr,
        )
        raise SystemExit(1)
