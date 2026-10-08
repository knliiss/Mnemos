#!/usr/bin/env python3
"""Provision only the three C7x credentials on the trusted production host.

Never print secret values, read request-branch code, or overwrite valid
credentials. The production env path is deliberately not configurable.
"""
from __future__ import annotations

import os
import re
import secrets
import stat
import subprocess
import tempfile
from datetime import datetime, timezone
from pathlib import Path

ENV = Path("/etc/mnemos/mnemos.env")
ROLE = "BOT_MANAGEMENT_POSTGRES_USER"
ROLE_VALUE = "mnemos_bot_management"
KEYS = (
    "BOT_MANAGEMENT_POSTGRES_PASSWORD",
    "REALTIME_SERVICE_BOT_MANAGEMENT_API_KEY",
    "BOT_MANAGEMENT_SERVICE_REALTIME_API_KEY",
)
ENTRY = re.compile(r"^([A-Za-z_][A-Za-z0-9_]*)=(.*)$")
SECRET = re.compile(r"(_PASSWORD|_API_KEY|_TOKEN|_CLIENT_SECRET|_SECRET_KEY|_PROJECT_KEY)$")


def replaceable(value: str) -> bool:
    return value in ("", "replace_me", "change_me", "changeme", "replace-me") or (
        value.startswith("replace_me_") or value.startswith("change_me_")
    )


def main() -> None:
    if os.geteuid() != 0:
        raise SystemExit("Root required for production secret provisioning")
    if ENV.is_symlink() or not ENV.is_file():
        raise SystemExit("Production env file is missing or is a symlink")
    info = ENV.stat()
    if info.st_uid != 0 or not stat.S_ISREG(info.st_mode) or info.st_mode & 0o077:
        raise SystemExit("Production env must be a root-owned regular file, mode 0600")
    original = ENV.read_text(encoding="utf-8")
    values: dict[str, str] = {}
    lines = original.splitlines(keepends=True)
    for line in lines:
        match = ENTRY.fullmatch(line.rstrip("\r\n"))
        if match:
            key, value = match.groups()
            if key in values:
                raise SystemExit("Duplicate production key: " + key)
            values[key] = value

    if ROLE in values and values[ROLE] != ROLE_VALUE:
        raise SystemExit("Existing bot-management database role differs from approved role")
    additions = {}
    if ROLE not in values:
        additions[ROLE] = ROLE_VALUE
    for key in KEYS:
        current = values.get(key, "")
        if replaceable(current):
            additions[key] = secrets.token_hex(32)
        elif len(current) < 16:
            raise SystemExit("Existing " + key + " is too short; refusing to rotate")

    existing = {v for k, v in values.items() if SECRET.search(k) and not replaceable(v)}
    for key in KEYS:
        value = additions.get(key, values.get(key, ""))
        if value in existing and key in additions:
            raise SystemExit("Credential collision for " + key)
        existing.add(value)
    # Existing strong keys are left unchanged; validate all together before writing.
    replacements = dict(additions)
    output = []
    for line in lines:
        match = ENTRY.fullmatch(line.rstrip("\r\n"))
        if match and match.group(1) in replacements:
            key = match.group(1)
            output.append(key + "=" + replacements[key] + "\n")
        else:
            output.append(line)
    if output and not output[-1].endswith("\n"):
        output[-1] += "\n"
    for key, value in replacements.items():
        if key not in values:
            output.append(key + "=" + value + "\n")
    proposed = "".join(output)

    validator = Path(__file__).resolve().with_name("validate-production-env.sh")
    if not validator.is_file():
        raise SystemExit("Trusted production env validator is unavailable")
    fd, filename = tempfile.mkstemp(prefix=".mnemos-c7x-env-", dir=ENV.parent)
    staged = Path(filename)
    try:
        os.fchmod(fd, 0o600)
        with os.fdopen(fd, "w", encoding="utf-8") as handle:
            handle.write(proposed)
            handle.flush()
            os.fsync(handle.fileno())
        check_env = os.environ.copy()
        check_env["MNEMOS_PRODUCTION_ENV_FILE"] = str(staged)
        subprocess.run(["sh", str(validator)], env=check_env, check=True)
        if proposed != original:
            timestamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
            backup = ENV.with_name(ENV.name + ".backup-c7x-" + timestamp)
            backup_fd = os.open(backup, os.O_CREAT | os.O_EXCL | os.O_WRONLY, 0o600)
            with os.fdopen(backup_fd, "w", encoding="utf-8") as handle:
                handle.write(original)
                handle.flush()
                os.fsync(handle.fileno())
            os.replace(staged, ENV)
        print("C7x production secrets verified; actual values are not logged.")
        print("Verified keys: " + ", ".join((ROLE, *KEYS)))
    finally:
        staged.unlink(missing_ok=True)


if __name__ == "__main__":
    main()
