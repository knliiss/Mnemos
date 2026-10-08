#!/usr/bin/env python3
"""Delete only stale, non-production GitHub Actions artifacts.

Invoked from a protected, immutable master workflow through an audited request.
No workflow inputs may change the retention window or deletion allowlist.
"""
from __future__ import annotations

import datetime as dt
import json
import os
import re
import sys
import urllib.error
import urllib.request

REPOSITORY = "knliiss/Mnemos"
AGE_DAYS = 3
MAX_DELETES = 350
MAX_PAGES = 40
PROTECTED = (
    "production", "release", "deploy", "audit", "plan", "snapshot",
    "backup", "rollback", "security", "attestation", "manifest", "recovery",
)
TOKEN = os.environ.get("GH_TOKEN", "")
if not TOKEN:
    raise SystemExit("No GitHub Actions token available")

HEADERS = {
    "Authorization": "Bearer " + TOKEN,
    "Accept": "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28",
}
BASE = "https://api.github.com/repos/" + REPOSITORY
CUTOFF = dt.datetime.now(dt.timezone.utc) - dt.timedelta(days=AGE_DAYS)


def request(url: str, method: str = "GET") -> dict:
    req = urllib.request.Request(url, headers=HEADERS, method=method)
    try:
        with urllib.request.urlopen(req, timeout=40) as response:
            body = response.read()
        return json.loads(body) if body else {}
    except urllib.error.HTTPError as error:
        raise RuntimeError(f"GitHub artifact {method} failed with HTTP {error.code}") from None


def parse_time(value: str) -> dt.datetime:
    return dt.datetime.fromisoformat(value.replace("Z", "+00:00"))


def eligible(artifact: dict) -> bool:
    name = str(artifact.get("name", "")).lower()
    if not name or any(part in name for part in PROTECTED):
        return False
    try:
        artifact_id = int(artifact["id"])
        created = parse_time(str(artifact["created_at"]))
    except (ValueError, KeyError, TypeError):
        return False
    return artifact_id > 0 and created < CUTOFF


def main() -> None:
    found = []
    total_count = None
    for page in range(1, MAX_PAGES + 1):
        result = request(f"{BASE}/actions/artifacts?per_page=100&page={page}")
        if total_count is None:
            total_count = result.get("total_count", 0)
        batch = result.get("artifacts", [])
        if not isinstance(batch, list):
            raise RuntimeError("Unexpected GitHub artifact response")
        found.extend(batch)
        if len(batch) < 100:
            break
    else:
        raise RuntimeError("Artifact collection exceeded enumeration limit; no deletion attempted")
    selected = [artifact for artifact in found if eligible(artifact)]
    if len(selected) > MAX_DELETES:
        selected = sorted(selected, key=lambda a: a["created_at"])[:MAX_DELETES]
    removed = 0
    bytes_removed = 0
    for artifact in selected:
        artifact_id = int(artifact["id"])
        request(f"{BASE}/actions/artifacts/{artifact_id}", method="DELETE")
        removed += 1
        bytes_removed += int(artifact.get("size_in_bytes", 0))
    print(f"Artifact scan: {len(found)} entries; {total_count} reported in repository")
    print(f"Removed {removed} stale non-production artifacts older than {AGE_DAYS} days")
    print(f"Freed approximately {bytes_removed / (1024 * 1024):.1f} MiB (quota recalculation may be delayed)")
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as stream:
            stream.write(
                "## Audited CI artifact cleanup\n\n"
                f"- Scanned: {len(found)} artifacts\n"
                f"- Deleted: {removed} artifacts older than {AGE_DAYS} days\n"
                f"- Approximate deleted size: {bytes_removed / (1024 * 1024):.1f} MiB\n"
                "- Protected production, release, deployment, audit, plan, backup, "
                "snapshot, and recovery artifacts were excluded.\n"
            )


if __name__ == "__main__":
    try:
        main()
    except (RuntimeError, ValueError) as exc:
        print(f"Artifact cleanup stopped: {exc}", file=sys.stderr)
        raise SystemExit(1)
