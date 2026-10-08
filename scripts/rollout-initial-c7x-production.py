#!/usr/bin/env python3
"""Scoped, one-time 16->17-service bootstrap. Trusted master/production runner only."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

sys.dont_write_bytecode = True
from production_deployment_state import build_state, digest, state_identity
from production_release_manifest import build_manifest, manifest_id, service_entry
from production_runtime_state import inspect_runtime_image, strip_swarm_digest
from production_service_catalog import services as catalog_services

ROOT = Path(__file__).resolve().parent.parent
STATE = Path("/var/lib/mnemos-deploy/current-state.json")
ENV = Path("/etc/mnemos/mnemos.env")
CURRENT = Path("/opt/mnemos/current")
TAG = os.environ.get("C7X_ROLLOUT_SHA", "")
REGISTRY = "ghcr.io/knliiss"
NEW = "bot-management-service"
TARGETS = ("bot-management-service", "gateway-service", "realtime-service")
EXPECTED = {
    "gateway-service": "ed238fc8e0fb538b941a0d79cc1d4533625cc2ea",
    "realtime-service": "86c7a654af20f26a25aaeaf3f126a2639f0ed7d6",
}

class Blocked(RuntimeError):
    pass

def require(condition, message):
    if not condition:
        raise Blocked(message)

def call(cmd, label, env=None):
    p = subprocess.run(cmd, cwd=ROOT, env=env, text=True, capture_output=True)
    if p.returncode:
        # NEVER print subprocess args, env or stderr: can expose live credentials.
        raise Blocked(f"{label} failed (exit {p.returncode})")
    return p.stdout.strip()

def docker(*args):
    return call(["docker", *args], "Docker operation")

def load_env():
    values = {}
    require(ENV.is_file() and not ENV.is_symlink(), "Production env not found")
    for line in ENV.read_text().splitlines():
        if line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        require(key not in values, "Duplicate environment variable: " + key)
        values[key] = value.strip().strip('"').strip("'")
    for key in ("BOT_MANAGEMENT_POSTGRES_PASSWORD",
                "REALTIME_SERVICE_BOT_MANAGEMENT_API_KEY",
                "BOT_MANAGEMENT_SERVICE_REALTIME_API_KEY"):
        require(bool(re.fullmatch(r"[a-f0-9]{64}", values.get(key, ""))), "Invalid secret: " + key)
    require(values.get("BOT_MANAGEMENT_POSTGRES_USER") == "mnemos_bot_management",
            "Unexpected bot-management database role")
    return values

def recorded_image(name, spec):
    return strip_swarm_digest(inspect_runtime_image(name, spec))

def readiness(name, expected, limit=240):
    full = "mnemos_" + name
    started = time.monotonic()
    stable = None
    while time.monotonic() - started < limit:
        raw = docker("service", "ls", "--filter", "name=" + full,
                     "--format", "{{.Name}} {{.Replicas}}")
        row = next((x.split() for x in raw.splitlines() if x.startswith(full + " ")), None)
        if row and len(row) == 2 and "/" in row[1]:
            up, desired = row[1].split("/", 1)
            if desired != "0" and up == desired:
                image = docker("service", "inspect", "--format",
                               "{{.Spec.TaskTemplate.ContainerSpec.Image}}", full)
                if strip_swarm_digest(image) == expected:
                    stable = stable or time.monotonic()
                    if time.monotonic() - stable >= 15:
                        print("Converged:", name)
                        return
                else:
                    stable = None
            else:
                stable = None
        time.sleep(5)
    raise Blocked("Service failed to converge: " + name)

def env_before(name):
    raw = docker("service", "inspect", "--format",
                 "{{json .Spec.TaskTemplate.ContainerSpec.Env}}", "mnemos_" + name)
    return dict(item.split("=", 1) for item in (json.loads(raw) or []))

def patch(name, image, values):
    cmd = ["service", "update", "--detach=true", "--with-registry-auth"]
    for key, value in values.items():
        cmd.extend(["--env-add", key + "=" + value])
    cmd.extend(["--image", image, "mnemos_" + name])
    docker(*cmd)
    readiness(name, image)

def rollback(touched, old_images, old_env, new_created):
    ok = True
    for name, keys in reversed(touched):
        cmd = ["service", "update", "--detach=true", "--with-registry-auth"]
        for key in keys:
            if key in old_env[name]:
                cmd.extend(["--env-add", key + "=" + old_env[name][key]])
            else:
                cmd.extend(["--env-rm", key])
        cmd.extend(["--image", old_images[name], "mnemos_" + name])
        try:
            docker(*cmd)
            readiness(name, old_images[name])
        except Exception:
            print("CRITICAL: Cannot restore " + name, file=sys.stderr)
            ok = False
    if new_created:
        probe = subprocess.run(["docker", "service", "inspect", "mnemos_" + NEW],
                               stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if probe.returncode == 0:
            try:
                docker("service", "rm", "mnemos_" + NEW)
            except Exception:
                print("CRITICAL: Cannot remove new bot-management service", file=sys.stderr)
                ok = False
    return ok

def initialize_bot_database(values):
    """Only provision the bot role/schema: never rotate or touch other service roles."""
    user = values.get("APP_POSTGRES_USER", "")
    database = values.get("APP_POSTGRES_DB", "")
    admin_password = values.get("APP_POSTGRES_PASSWORD", "")
    require(bool(re.fullmatch(r"[a-z_][a-z0-9_]*", user)), "Invalid PostgreSQL admin role")
    require(bool(re.fullmatch(r"[a-z_][a-z0-9_]*", database)), "Invalid PostgreSQL DB name")
    require(bool(admin_password), "Missing PostgreSQL admin password")
    # All dynamic SQL content is a validated 64-character hexadecimal secret.
    password = values["BOT_MANAGEMENT_POSTGRES_PASSWORD"]
    sql = f"""
DO $bootstrap$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'mnemos_bot_management') THEN
    CREATE ROLE mnemos_bot_management LOGIN PASSWORD '{password}'
      NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS;
  END IF;
END $bootstrap$;
ALTER ROLE mnemos_bot_management LOGIN PASSWORD '{password}'
  NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS;
CREATE SCHEMA IF NOT EXISTS bot_management AUTHORIZATION mnemos_bot_management;
ALTER SCHEMA bot_management OWNER TO mnemos_bot_management;
REVOKE ALL ON SCHEMA bot_management FROM PUBLIC;
"""
    result = subprocess.run(
        ["docker", "exec", "-i", "-e", "PGPASSWORD=" + admin_password,
         "mnemos-app-postgres", "psql", "-X", "-q", "-v", "ON_ERROR_STOP=1",
         "-U", user, "-d", database],
        input=sql, cwd=ROOT, text=True, capture_output=True,
    )
    if result.returncode:
        raise Blocked(f"Isolated bot PostgreSQL provisioning failed (exit {result.returncode})")
    print("Isolated bot_management role and schema initialized without other role changes")

def initial_manifest(catalog, old):
    services = dict(old)
    for name in TARGETS:
        services[name] = service_entry(name, catalog[name], REGISTRY, TAG)
    return build_manifest(services)

def main():
    require(os.geteuid() == 0, "Root required")
    require(bool(re.fullmatch(r"[0-9a-f]{40}", TAG)), "Invalid trusted master SHA")
    require(STATE.is_file() and STATE.stat().st_uid == 0, "Canonical state missing")
    call(["sh", "scripts/validate-production-env.sh"], "Production env validation")
    prior_runtime = str(CURRENT.resolve(strict=True))
    call(["sh", prior_runtime + "/scripts/verify-production-availability.sh"], "Old baseline readiness")
    call(["sh", prior_runtime + "/scripts/check-production-health.sh"], "Old baseline health")
    require(docker("info", "--format", "{{.Swarm.ControlAvailable}}") == "true",
            "Not a swarm manager")
    require(docker("network", "inspect", "--format", "{{.Driver}}/{{.Attachable}}",
                   "mnemos-production-network") == "overlay/true", "Production overlay unavailable")
    secrets = load_env()
    cat = catalog_services()
    old_state = json.loads(STATE.read_text())
    original = old_state["release_manifest"]
    old = original["services"]
    deployed = {name for name, value in cat.items() if isinstance(value.get("deploy"), dict)}
    require(set(old) == deployed - {NEW} and len(old) == 16,
            "Production must have exactly 16 known deployable services, without bot management")
    require(original["manifest_id"] == manifest_id(old), "Legacy manifest digest mismatch")
    require(old_state["state_id"] == digest(state_identity(old_state["tooling_revision"], original)),
            "Legacy canonical state digest mismatch")
    for name, entry in old.items():
        require(entry.get("deploy") == cat[name]["deploy"] and
                entry.get("kind") == cat[name]["kind"], "Changed legacy catalog: " + name)
        require(recorded_image(name, cat[name]) == entry.get("image"),
                "Production drift: " + name)
    for name, expected in EXPECTED.items():
        require(old[name]["source_sha"] == expected, "Unexpected baseline: " + name)
    p = subprocess.run(["docker", "service", "inspect", "mnemos_" + NEW],
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    require(p.returncode != 0, "Bot service already present; cannot repeat initial bootstrap")
    for name in TARGETS:
        docker("image", "inspect", f"{REGISTRY}/{cat[name]['image']}:{TAG}")
    old_images = {name: old[name]["image"] for name in EXPECTED}
    old_environ = {name: env_before(name) for name in EXPECTED}
    manifest = initial_manifest(cat, old)
    desired = build_state(TAG, manifest)
    for name in old:
        if name not in EXPECTED:
            require(old[name] == manifest["services"][name], "Unexpected unrelated image change")
    with tempfile.TemporaryDirectory(prefix="c7x-initial-") as directory:
        work = Path(directory)
        work.chmod(0o700)
        manifest_file = work / "manifest.json"
        full_file = work / "full.json"
        bot_file = work / "bot.json"
        manifest_file.write_text(json.dumps(manifest))
        render_env = dict(os.environ)
        render_env.update({
            "MNEMOS_IMAGE_REGISTRY": REGISTRY, "MNEMOS_RELEASE_TAG": TAG,
            "MNEMOS_SWARM_NETWORK": "mnemos-production-network",
            "MNEMOS_PRODUCTION_ENV_FILE": str(ENV),
            "MNEMOS_KAFKA_TRUSTSTORE_CONFIG": "unreferenced-c7x-bootstrap",
            "MNEMOS_CLOUDFLARE_TUNNEL_SECRET": "unreferenced-c7x-bootstrap",
        })
        call(["sh", "scripts/render-production-app-stack.sh",
              str(full_file), str(manifest_file)], "Render validated production service", render_env)
        stack = json.loads(full_file.read_text())
        bot = stack["services"][NEW]
        require(bot["image"] == manifest["services"][NEW]["image"], "Rendered bot image mismatch")
        # The common production renderer mounts Kafka truststore on every app.
        # This service has no Kafka client, so omit this otherwise non-existent
        # placeholder config from the isolated first-install stack only.
        configured = bot.pop("configs", [])
        require(len(configured) == 1 and
                configured[0].get("source") == "kafka_truststore" and
                configured[0].get("target") == "/etc/mnemos/kafka/kafka.truststore.p12",
                "Unexpected bot configs; refusing to omit any unrelated config")
        require(not bot.get("secrets"), "Bot unexpectedly references secrets")
        require(bot.get("networks") == ["mnemos-network"], "Unexpected bot network")
        network = stack["networks"]["mnemos-network"]
        require(network == {"external": True, "name": "mnemos-production-network"},
                "Unexpected external network")
        bot_file.write_text(json.dumps({
            "version": "3.8", "services": {NEW: bot},
            "networks": {"mnemos-network": network},
        }))
        call(["docker", "stack", "config", "--compose-file", str(bot_file)],
             "Validate isolated bot service compose")
        print("PRE-FLIGHT PASSED: 16 original images unchanged, 3 immutable images available.")
        created = False
        touched = []
        wrote_state = False
        installed_runtime = False
        backup = STATE.with_name("before-c7x-" + str(int(time.time())) + ".json")
        try:
            initialize_bot_database(secrets)
            created = True  # Docker may create the service before returning a failed exit code.
            docker("stack", "deploy", "--compose-file", str(bot_file),
                   "--with-registry-auth", "--resolve-image", "always", "mnemos")
            readiness(NEW, manifest["services"][NEW]["image"])
            gateway = {"BOT_MANAGEMENT_SERVICE_URL": "http://bot-management-service:8095"}
            touched.append(("gateway-service", tuple(gateway)))
            patch("gateway-service", manifest["services"]["gateway-service"]["image"], gateway)
            realtime = {
                "MNEMOS_BOT_MANAGEMENT_URL": "http://bot-management-service:8095",
                "REALTIME_SERVICE_BOT_MANAGEMENT_API_KEY": secrets["REALTIME_SERVICE_BOT_MANAGEMENT_API_KEY"],
                "BOT_MANAGEMENT_SERVICE_REALTIME_API_KEY": secrets["BOT_MANAGEMENT_SERVICE_REALTIME_API_KEY"],
            }
            touched.append(("realtime-service", tuple(realtime)))
            patch("realtime-service", manifest["services"]["realtime-service"]["image"], realtime)
            for name, entry in old.items():
                if name not in EXPECTED:
                    require(recorded_image(name, cat[name]) == entry["image"],
                            "Unrelated runtime image changed: " + name)
            shutil.copy2(STATE, backup)
            backup.chmod(0o600)
            new_path = work / "state.json"
            new_path.write_text(json.dumps(desired, indent=2) + "\n")
            os.replace(new_path, STATE)
            wrote_state = True
            installed_runtime = True  # Restore the symlink even if installation fails halfway.
            call(["sh", "scripts/install-production-runtime.sh", TAG], "Install verified tooling")
            call(["python3", "scripts/production_runtime_state.py",
                  "verify", "--state", str(STATE)], "Verify canonical 17 service state")
            call(["sh", "scripts/verify-production-availability.sh"], "Final availability")
            call(["sh", "scripts/check-production-health.sh"], "Final production health")
            call(["sh", "scripts/verify-production-telegram-poller.sh"], "Telegram singleton health")
            check = subprocess.run(["curl", "--silent", "--show-error", "--output", "/dev/null",
                                    "--write-out", "%{http_code}", "--max-time", "15",
                                    "https://api.knalis.rest/api/v1/bot-management/admin/bot-agents"],
                                   capture_output=True, text=True)
            require(check.returncode == 0 and check.stdout.strip() in {"401", "403"},
                    "Public bot management API did not return protected-route status")
            print("C7X INITIAL BACKEND PRODUCTION DEPLOY COMPLETE")
            print("All 17 runtime service images match canonical state; API route protected.")
        except Exception:
            print("Initial rollout failed; attempting scoped rollback.", file=sys.stderr)
            recovered = rollback(touched, old_images, old_environ, created)
            if wrote_state:
                try:
                    shutil.copy2(backup, STATE)
                    STATE.chmod(0o600)
                    if installed_runtime:
                        restore_link = CURRENT.with_name("current.restore-c7x")
                        restore_link.unlink(missing_ok=True)
                        restore_link.symlink_to(prior_runtime)
                        os.replace(restore_link, CURRENT)
                except Exception:
                    recovered = False
            print("Scoped rollback succeeded" if recovered else
                  "CRITICAL: Scoped rollback incomplete; manual recovery required", file=sys.stderr)
            raise

if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        # Print only controlled errors; subprocess arguments may contain API keys.
        detail = str(error) if isinstance(error, Blocked) else type(error).__name__
        print("C7x initial rollout: " + detail, file=sys.stderr)
        raise SystemExit(1)
