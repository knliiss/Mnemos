#!/usr/bin/env python3

from __future__ import annotations

import json
import os
import subprocess
import sys
import tempfile
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
RENDERER = REPOSITORY_ROOT / "scripts" / "render-production-stack.py"

APPLICATION_PORTS = {
    "account-service": 8081,
    "wallet-service": 8082,
    "event-service": 8083,
    "notification-service": 8084,
    "reward-service": 8085,
    "bet-service": 8086,
    "moderation-service": 8087,
    "realtime-service": 8088,
    "personalization-service": 8089,
    "social-service": 8090,
    "profile-service": 8091,
    "player-directory-service": 8092,
    "payment-service": 8093,
    "gateway-service": 8080,
}

REPLICA_SAFE_SERVICES = {
    "account-service",
    "event-service",
    "gateway-service",
    "payment-service",
    "wallet-service",
}


def fail(message: str) -> None:
    raise AssertionError(message)


def source_model() -> dict[str, object]:
    services: dict[str, object] = {}

    for service_name, port in APPLICATION_PORTS.items():
        services[service_name] = {
            "build": {
                "context": ".",
            },
            "container_name": f"mnemos-{service_name}",
            "depends_on": {
                "kafka": {
                    "condition": "service_healthy",
                }
            },
            "environment": {
                "SERVER_PORT": str(port),
                "EXAMPLE_SETTING": "preserved",
            },
            "image": f"mnemos/{service_name}:local",
            "networks": {
                "mnemos-network": None,
            },
            "ports": [
                {
                    "target": port,
                    "published": port,
                }
            ],
            "restart": "unless-stopped",
            "user": "10001:10001",
            "read_only": True,
            "cap_drop": ["ALL"],
            "security_opt": ["no-new-privileges:true"],
            "tmpfs": ["/tmp:rw,noexec,nosuid,size=64m"],
            "volumes": [
                {
                    "type": "volume",
                    "source": "kafka-client-tls",
                    "target": "/etc/mnemos/kafka",
                    "read_only": True,
                }
            ],
        }

    return {
        "name": "mnemos",
        "services": services,
        "networks": {
            "mnemos-network": {
                "name": "mnemos-network",
            }
        },
        "volumes": {
            "kafka-client-tls": {
                "name": "mnemos-kafka-client-tls",
            }
        },
    }


def render() -> dict[str, object]:
    environment = os.environ.copy()
    environment.update(
        {
            "MNEMOS_IMAGE_REGISTRY": "ghcr.io/knliiss",
            "MNEMOS_RELEASE_TAG": "0123456789abcdef",
            "MNEMOS_SWARM_NETWORK": "mnemos-production-network",
            "MNEMOS_KAFKA_TRUSTSTORE_CONFIG": "mnemos-kafka-truststore-deadbeef",
        }
    )

    with tempfile.TemporaryDirectory() as directory:
        input_path = Path(directory) / "compose.json"
        output_path = Path(directory) / "stack.json"

        input_path.write_text(
            json.dumps(source_model()),
            encoding="utf-8",
        )

        result = subprocess.run(
            [
                sys.executable,
                str(RENDERER),
                "--input",
                str(input_path),
                "--output",
                str(output_path),
            ],
            cwd=REPOSITORY_ROOT,
            env=environment,
            check=False,
            capture_output=True,
            text=True,
        )

        if result.returncode != 0:
            fail(
                "Renderer failed:\n"
                + result.stdout
                + result.stderr
            )

        return json.loads(output_path.read_text(encoding="utf-8"))


def verify_service(
    service_name: str,
    service: dict[str, object],
) -> None:
    expected_image = f"ghcr.io/knliiss/mnemos-{service_name}:0123456789abcdef"

    if service.get("image") != expected_image:
        fail(f"{service_name} does not use the immutable GHCR image")

    for unsupported_field in (
        "build",
        "container_name",
        "depends_on",
        "ports",
        "restart",
    ):
        if unsupported_field in service:
            fail(f"{service_name} still contains {unsupported_field}")

    if service.get("user") != "10001:10001":
        fail(f"{service_name} lost the non-root user")

    if service.get("read_only") is not True:
        fail(f"{service_name} lost read-only root filesystem")

    if service.get("cap_drop") != ["ALL"]:
        fail(f"{service_name} lost capability dropping")

    environment = service.get("environment")

    if not isinstance(environment, dict):
        fail(f"{service_name} environment is not normalized")

    if environment.get("EXAMPLE_SETTING") != "preserved":
        fail(f"{service_name} lost source environment")

    if environment.get("SERVER_SHUTDOWN") != "graceful":
        fail(f"{service_name} does not enable graceful shutdown")

    if environment.get("MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED") != "true":
        fail(f"{service_name} does not enable health probes")

    configs = service.get("configs")

    if not isinstance(configs, list) or len(configs) != 1:
        fail(f"{service_name} does not mount exactly one Kafka truststore config")

    config = configs[0]

    if not isinstance(config, dict):
        fail(f"{service_name} Kafka truststore config is invalid")

    if config.get("source") != "kafka_truststore":
        fail(f"{service_name} Kafka truststore source is invalid")

    if config.get("target") != "/etc/mnemos/kafka/kafka.truststore.p12":
        fail(f"{service_name} Kafka truststore target is invalid")

    if service.get("volumes"):
        fail(f"{service_name} unexpectedly retains the Compose Kafka volume")

    healthcheck = service.get("healthcheck")

    if not isinstance(healthcheck, dict):
        fail(f"{service_name} does not define a healthcheck")

    test = healthcheck.get("test")

    if not isinstance(test, list) or "actuator/health/readiness" not in str(test):
        fail(f"{service_name} healthcheck does not use readiness")

    deploy = service.get("deploy")

    if not isinstance(deploy, dict):
        fail(f"{service_name} does not define Swarm deploy policy")

    expected_replicas = 2 if service_name in REPLICA_SAFE_SERVICES else 1
    expected_order = "start-first" if service_name in REPLICA_SAFE_SERVICES else "stop-first"

    if deploy.get("replicas") != expected_replicas:
        fail(f"{service_name} has an unexpected replica count")

    update_config = deploy.get("update_config")

    if not isinstance(update_config, dict):
        fail(f"{service_name} does not define update_config")

    if update_config.get("order") != expected_order:
        fail(f"{service_name} has an unsafe rolling update order")

    if update_config.get("failure_action") != "rollback":
        fail(f"{service_name} does not automatically roll back failed updates")


def main() -> int:
    stack = render()

    services = stack.get("services")

    if not isinstance(services, dict):
        fail("Rendered stack does not contain services")

    if set(services) != set(APPLICATION_PORTS):
        fail("Rendered stack service set does not match Mnemos applications")

    for service_name, service in services.items():
        if not isinstance(service, dict):
            fail(f"{service_name} is not a service object")

        verify_service(
            service_name=service_name,
            service=service,
        )

    network = stack.get("networks", {}).get("mnemos-network", {})

    if network.get("external") is not True:
        fail("Production Swarm network must be external")

    if network.get("name") != "mnemos-production-network":
        fail("Production Swarm network has an unexpected name")

    config = stack.get("configs", {}).get("kafka_truststore", {})

    if config.get("name") != "mnemos-kafka-truststore-deadbeef":
        fail("Kafka truststore must reference the immutable external Swarm config")

    print("Production Swarm stack renderer verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
