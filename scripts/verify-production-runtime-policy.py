#!/usr/bin/env python3

from __future__ import annotations

import importlib.util
import json
import subprocess
import sys
import tempfile
from pathlib import Path

import production_deployment_executor
import production_deployment_plan
import production_runtime_policy


ROOT = Path(__file__).resolve().parent.parent
NORMALIZER = ROOT / "scripts" / "normalize-production-swarm-stack.py"
RECONCILER = ROOT / "scripts" / "reconcile-production-runtime-policy.py"
OVERRIDES = ROOT / "infrastructure" / "production" / "compose.app-overrides.yml"
RUNTIME_POLICY_WORKFLOW = ROOT / ".github" / "workflows" / "production-runtime-policy.yml"
RUNTIME_POLICY_REQUEST_WORKFLOW = (
    ROOT / ".github" / "workflows" / "production-runtime-policy-request.yml"
)

EXPECTED_ACTIVE_ACTIVE = {
    "account-service",
    "event-service",
    "gateway-service",
    "moderation-service",
    "payment-service",
    "personalization-service",
    "player-directory-service",
    "wallet-service",
}


def fail(message: str) -> None:
    raise AssertionError(message)


def load_reconciler():
    specification = importlib.util.spec_from_file_location(
        "production_runtime_policy_reconciler",
        RECONCILER,
    )

    if specification is None or specification.loader is None:
        fail("Could not load production runtime policy reconciler")

    module = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(module)
    return module


def verify_classification() -> None:
    services = set(production_runtime_policy.APPLICATION_SERVICES)

    if set(production_runtime_policy.ACTIVE_ACTIVE_SERVICES) != EXPECTED_ACTIVE_ACTIVE:
        fail("Unexpected active-active production service classification")

    if set(production_runtime_policy.SINGLETON_SERVICES) != services - EXPECTED_ACTIVE_ACTIVE:
        fail("Unexpected singleton production service classification")

    for service_name in services:
        active_active = service_name in EXPECTED_ACTIVE_ACTIVE
        expected_replicas = 2 if active_active else 1
        expected_order = "start-first" if active_active else "stop-first"

        if production_runtime_policy.desired_replicas(service_name) != expected_replicas:
            fail(f"Unexpected replica policy for {service_name}")

        if production_runtime_policy.update_order(service_name) != expected_order:
            fail(f"Unexpected update policy for {service_name}")


def verify_ordering() -> None:
    actions = [
        {
            "service": service_name,
            "runtime": {"type": "swarm"},
        }
        for service_name in production_runtime_policy.APPLICATION_SERVICES
    ]
    ordered = [
        action["service"]
        for action in sorted(
            actions,
            key=production_deployment_plan.action_order,
        )
    ]

    if ordered[-2:] != ["gateway-service", "telegram-bot-service"]:
        fail("Gateway and Telegram bot must be the final scoped deployment actions")

    compose_action = {
        "service": "postgres",
        "runtime": {"type": "compose"},
    }

    if production_deployment_plan.action_order(compose_action)[0] != 0:
        fail("Compose runtime actions must remain before Swarm application actions")


def verify_moderation_pool() -> None:
    runtime_value = production_runtime_policy.runtime_environment_overrides(
        "moderation-service"
    ).get("SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE")

    if runtime_value != "4":
        fail("Moderation runtime Hikari pool must be 4")

    expected = (
        "  moderation-service:\n"
        "    environment:\n"
        "      <<: *production-datasource-defaults\n"
        "      SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: \"4\""
    )

    if expected not in OVERRIDES.read_text(encoding="utf-8"):
        fail("Production Compose moderation Hikari pool must be 4")


def verify_normalizer() -> None:
    services = {
        name: {
            "image": f"example.invalid/{name}:unchanged",
            "deploy": {
                "resources": {
                    "limits": {"memory": "640M"},
                    "reservations": {"memory": "192M"},
                }
            },
        }
        for name in production_runtime_policy.APPLICATION_SERVICES
    }

    with tempfile.TemporaryDirectory() as directory:
        source = Path(directory) / "source.json"
        target = Path(directory) / "target.json"
        source.write_text(json.dumps({"services": services}), encoding="utf-8")

        result = subprocess.run(
            [
                sys.executable,
                str(NORMALIZER),
                "--input",
                str(source),
                "--output",
                str(target),
            ],
            cwd=ROOT,
            check=False,
            capture_output=True,
            text=True,
        )

        if result.returncode != 0:
            fail(result.stdout + result.stderr)

        normalized = json.loads(target.read_text(encoding="utf-8"))

    for name in production_runtime_policy.APPLICATION_SERVICES:
        service = normalized["services"][name]
        deploy = service["deploy"]

        if service["image"] != f"example.invalid/{name}:unchanged":
            fail(f"Normalizer changed the image for {name}")

        if deploy["replicas"] != production_runtime_policy.desired_replicas(name):
            fail(f"Normalizer replica policy mismatch for {name}")

        if deploy["update_config"]["order"] != production_runtime_policy.update_order(name):
            fail(f"Normalizer update policy mismatch for {name}")


def verify_reconciler() -> None:
    reconciler = load_reconciler()
    moderation = reconciler.update_command(
        "moderation-service",
        "mnemos_moderation-service",
    )

    if "--image" in moderation:
        fail("Runtime policy reconciliation must not replace images")

    if moderation[moderation.index("--replicas") + 1] != "2":
        fail("Moderation runtime policy must use two replicas")

    if moderation[moderation.index("--update-order") + 1] != "start-first":
        fail("Moderation runtime policy must use start-first")

    if "SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=4" not in moderation:
        fail("Moderation runtime policy must apply Hikari pool=4")

    telegram = reconciler.update_command(
        "telegram-bot-service",
        "mnemos_telegram-bot-service",
    )

    if telegram[telegram.index("--replicas") + 1] != "1":
        fail("Telegram bot must remain a singleton")

    if telegram[telegram.index("--update-order") + 1] != "stop-first":
        fail("Telegram bot updates must not overlap pollers")

    cloudflared = reconciler.cloudflared_update_command("mnemos_cloudflared")

    if "--image" in cloudflared:
        fail("Cloudflared runtime reconciliation must not replace the image")

    if cloudflared[cloudflared.index("--args") + 1] != reconciler.CLOUDFLARED_COMMAND:
        fail("Cloudflared runtime reconciliation must apply the HTTP/2 command")

    current_cloudflared = {
        "TaskTemplate": {
            "ContainerSpec": {
                "Args": reconciler.CLOUDFLARED_COMMAND.split(),
            }
        }
    }
    legacy_cloudflared = {
        "TaskTemplate": {
            "ContainerSpec": {
                "Args": [
                    "tunnel",
                    "run",
                    "--token-file",
                    "/run/secrets/cloudflare_tunnel_token",
                ],
            }
        }
    }

    if not reconciler.cloudflared_policy_matches(current_cloudflared):
        fail("Cloudflared HTTP/2 runtime must be recognized as reconciled")

    if reconciler.cloudflared_policy_matches(legacy_cloudflared):
        fail("Legacy Cloudflare Tunnel transport must require reconciliation")

    if reconciler.CLOUDFLARED_COMMAND not in NORMALIZER.read_text(encoding="utf-8"):
        fail("Normalizer and runtime reconciler disagree on the cloudflared command")


def verify_workflow_workspace_hygiene() -> None:
    workflow = RUNTIME_POLICY_WORKFLOW.read_text(encoding="utf-8")
    ownership_repair = 'sudo -n chown -R "$(id -u):$(id -g)" "$GITHUB_WORKSPACE"'
    checkout = "- name: Checkout approved production tooling"

    if ownership_repair not in workflow:
        fail("Production runtime policy must repair persistent-runner workspace ownership")

    if workflow.index(ownership_repair) > workflow.index(checkout):
        fail("Production runner workspace ownership must be repaired before checkout")

    if workflow.count("PYTHONDONTWRITEBYTECODE=1") < 3:
        fail("Privileged production Python calls must disable bytecode writes")

    if "sudo -n python3 scripts/reconcile-production-runtime-policy.py" in workflow:
        fail("Runtime reconciliation must not create root-owned Python bytecode")

    if "sudo -n python3 scripts/production_runtime_state.py" in workflow:
        fail("Runtime state observation must not create root-owned Python bytecode")


def verify_workflow_env_reading() -> None:
    workflow = RUNTIME_POLICY_WORKFLOW.read_text(encoding="utf-8")

    if ". /etc/mnemos/mnemos.env" in workflow:
        fail("Production runtime policy must not source the full production env file")

    required_fragments = (
        "read_env_value()",
        'sudo -n awk -v key="$key"',
        'host_bind_address="$(read_env_value MNEMOS_HOST_BIND_ADDRESS)"',
        'keycloak_host_port="$(read_env_value KEYCLOAK_HOST_PORT)"',
        'moderation_client_id="$(read_env_value MNEMOS_MODERATION_SERVICE_CLIENT_ID)"',
        'moderation_client_secret="$(read_env_value KEYCLOAK_MODERATION_SERVICE_CLIENT_SECRET)"',
    )

    for fragment in required_fragments:
        if fragment not in workflow:
            fail(f"Production runtime policy is missing safe env read guard: {fragment}")


def verify_workflow_state_recording() -> None:
    workflow = RUNTIME_POLICY_WORKFLOW.read_text(encoding="utf-8")
    root_owned_temp = 'state_tmp="$(sudo -n mktemp)"'
    runner_owned_temp = 'state_tmp="$(mktemp)"'

    if root_owned_temp not in workflow:
        fail("Production runtime state recording must create its temp file as root")

    if runner_owned_temp in workflow:
        fail("Production runtime state recording must not pass a runner-owned /tmp file to root")


def verify_workflow_authorization_boundary() -> None:
    workflow = RUNTIME_POLICY_WORKFLOW.read_text(encoding="utf-8")
    request = RUNTIME_POLICY_REQUEST_WORKFLOW.read_text(encoding="utf-8")

    required_request_fragments = (
        "deploy/production-runtime-policy",
        "runs-on: ubuntu-24.04",
        "scripts/production_source_revision.py",
        "--trusted-ref origin/master",
    )

    for fragment in required_request_fragments:
        if fragment not in request:
            fail(f"Runtime policy request is missing authorization guard: {fragment}")

    if "mnemos-production" in request:
        fail("Runtime policy request must not have production runner access")

    for forbidden in (
        "scripts/production-infra.sh",
        "scripts/reconcile-production-runtime-policy.py",
        "scripts/install-production-runtime.sh",
        "docker service update",
        "docker stack deploy",
        "/etc/mnemos",
    ):
        if forbidden in request:
            fail(f"Runtime policy request contains production mutation capability: {forbidden}")

    if "\n  push:\n" in workflow:
        fail("Production runtime mutation workflow must not run directly from deploy ref pushes")

    required_runtime_fragments = (
        "workflow_run:",
        "Production Runtime Policy Request",
        ".github/workflows/production-runtime-policy-request.yml",
        "actions: read",
        "scripts/production_source_revision.py",
        "--trusted-ref origin/master",
        "scripts/production-infra.sh",
        "keycloak-config",
        "mnemos_user_id:__mnemos_permission_probe__",
        "verify-production-telegram-poller.sh",
    )

    for fragment in required_runtime_fragments:
        if fragment not in workflow:
            fail(f"Production runtime policy is missing required guard: {fragment}")

    request_validation = workflow.index("- name: Validate explicit production authorization")
    keycloak_reconcile = workflow.index("- name: Reconcile declarative Keycloak configuration")

    if request_validation > keycloak_reconcile:
        fail("Keycloak reconciliation must occur only after request authorization")


def verify_executor() -> None:
    steps = production_deployment_executor.availability_steps()

    if len(steps) != 1:
        fail("Scoped executor must define exactly one availability guard step")

    command = steps[0].get("command", [])

    if not command or not str(command[-1]).endswith("verify-production-availability.sh"):
        fail("Scoped executor does not use the production availability guard")


def main() -> int:
    try:
        verify_classification()
        verify_ordering()
        verify_moderation_pool()
        verify_normalizer()
        verify_reconciler()
        verify_workflow_workspace_hygiene()
        verify_workflow_env_reading()
        verify_workflow_state_recording()
        verify_workflow_authorization_boundary()
        verify_executor()
    except (AssertionError, OSError, ValueError) as exception:
        print(f"Production runtime policy verification failed: {exception}", file=sys.stderr)
        return 1

    print("Production zero-downtime runtime policy verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
