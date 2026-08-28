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
    "bet-service",
    "event-service",
    "gateway-service",
    "moderation-service",
    "notification-service",
    "payment-service",
    "personalization-service",
    "profile-service",
    "realtime-service",
    "reward-service",
    "social-service",
    "wallet-service",
}

CLOUDFLARED_IMAGE = "cloudflare/cloudflared:2026.8.1"
CLOUDFLARE_SECRET_NAME = "mnemos-cloudflare-tunnel-token-deadbeef"


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
            "MNEMOS_CLOUDFLARE_TUNNEL_SECRET": CLOUDFLARE_SECRET_NAME,
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


def verify_application_service(
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


def verify_cloudflared(service: dict[str, object]) -> None:
    if service.get("image") != CLOUDFLARED_IMAGE:
        fail("cloudflared does not use the pinned production image")

    if service.get("user") != "65532:65532":
        fail("cloudflared must run as the distroless non-root user")

    if service.get("read_only") is not True:
        fail("cloudflared must use a read-only root filesystem")

    if service.get("cap_drop") != ["ALL"]:
        fail("cloudflared must drop all Linux capabilities")

    if "ports" in service:
        fail("cloudflared must not publish host ports")

    command = service.get("command")

    if not isinstance(command, list):
        fail("cloudflared command must use exec form")

    if command != [
        "tunnel",
        "run",
        "--token-file",
        "/run/secrets/cloudflare_tunnel_token",
    ]:
        fail("cloudflared must read the tunnel token only from the Swarm secret file")

    secrets = service.get("secrets")

    if not isinstance(secrets, list) or len(secrets) != 1:
        fail("cloudflared must mount exactly one tunnel secret")

    secret = secrets[0]

    if not isinstance(secret, dict):
        fail("cloudflared tunnel secret mapping is invalid")

    if secret.get("source") != "cloudflare_tunnel_token":
        fail("cloudflared tunnel secret source is invalid")

    if secret.get("target") != "/run/secrets/cloudflare_tunnel_token":
        fail("cloudflared tunnel secret target is invalid")

    if secret.get("uid") != "65532" or secret.get("gid") != "65532":
        fail("cloudflared tunnel secret ownership is invalid")

    if secret.get("mode") != 0o400:
        fail("cloudflared tunnel secret must be owner-readable only")

    deploy = service.get("deploy")

    if not isinstance(deploy, dict):
        fail("cloudflared does not define Swarm deploy policy")

    if deploy.get("replicas") != 2:
        fail("cloudflared must run with two replicas")

    update_config = deploy.get("update_config")

    if not isinstance(update_config, dict) or update_config.get("order") != "start-first":
        fail("cloudflared must use start-first rolling updates")


def main() -> int:
    stack = render()

    services = stack.get("services")

    if not isinstance(services, dict):
        fail("Rendered stack does not contain services")

    expected_services = set(APPLICATION_PORTS) | {"cloudflared"}

    if set(services) != expected_services:
        fail("Rendered stack service set does not match Mnemos production services")

    for service_name in APPLICATION_PORTS:
        service = services.get(service_name)

        if not isinstance(service, dict):
            fail(f"{service_name} is not a service object")

        verify_application_service(
            service_name=service_name,
            service=service,
        )

    cloudflared = services.get("cloudflared")

    if not isinstance(cloudflared, dict):
        fail("cloudflared is not a service object")

    verify_cloudflared(cloudflared)

    network = stack.get("networks", {}).get("mnemos-network", {})

    if network.get("external") is not True:
        fail("Production Swarm network must be external")

    if network.get("name") != "mnemos-production-network":
        fail("Production Swarm network has an unexpected name")

    config = stack.get("configs", {}).get("kafka_truststore", {})

    if config.get("name") != "mnemos-kafka-truststore-deadbeef":
        fail("Kafka truststore must reference the immutable external Swarm config")

    tunnel_secret = stack.get("secrets", {}).get("cloudflare_tunnel_token", {})

    if tunnel_secret.get("external") is not True:
        fail("Cloudflare Tunnel token must be an external Swarm secret")

    if tunnel_secret.get("name") != CLOUDFLARE_SECRET_NAME:
        fail("Cloudflare Tunnel secret name is unexpected")

    serialized = json.dumps(stack)

    if "TUNNEL_TOKEN" in serialized:
        fail("Rendered stack must not expose a Cloudflare Tunnel token environment variable")

    print("Production Swarm stack renderer verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
