#!/usr/bin/env python3
"""Read-only production Telegram diagnostics. Never prints service secrets or raw bot logs."""
import datetime as dt
import json
import os
import re
import subprocess
import urllib.error
import urllib.request

SERVICE = "mnemos_telegram-bot-service"
lines = [
    "# Production Telegram poller diagnostic",
    "",
    f"Run: {os.environ.get('RUN_URL', 'unknown')}",
    f"Observed at: {dt.datetime.now(dt.timezone.utc).isoformat()}",
    "",
]

def docker(*args):
    try:
        p = subprocess.run(
            ["sudo", "-n", "docker", *args],
            text=True, capture_output=True, timeout=40, check=False,
        )
        return p.stdout if p.returncode == 0 else None
    except (OSError, subprocess.TimeoutExpired):
        return None

def add(label, value):
    lines.append(f"- {label}: `{value}`")

services = docker("service", "ls", "--filter", f"name={SERVICE}",
                  "--format", "{{.Name}}|{{.Replicas}}")
service_entries = [row.split("|") for row in (services or "").splitlines()
                   if row.split("|")[0] == SERVICE]
replicas = service_entries[0][1] if service_entries else "not found"
lines.append("## Live Docker Swarm")
add("Service name", SERVICE)
add("Replica count", replicas)
add("Expected 1/1 singleton", replicas == "1/1")
tasks = docker("service", "ps", SERVICE, "--format",
               "{{.Name}}|{{.CurrentState}}|{{.DesiredState}}")
if tasks is not None:
    for row in tasks.splitlines()[:8]:
        add("Task state", re.sub(r"[^A-Za-z0-9| _.:/-]", "", row)[:140])

raw_env = docker("service", "inspect", SERVICE,
                 "--format", "{{json .Spec.TaskTemplate.ContainerSpec.Env}}")
env = {}
if raw_env:
    try:
        env = dict(field.split("=", 1) for field in json.loads(raw_env)
                   if "=" in field)
    except (ValueError, TypeError):
        pass
token = env.get("MNEMOS_TELEGRAM_BOT_TOKEN", "")
lines.append("")
lines.append("## Polling configuration")
add("TELEGRAM_BOT_ENABLED", env.get("TELEGRAM_BOT_ENABLED", "not found"))
add("Token configured", bool(token))

logs = docker("service", "logs", "--raw", "--timestamps", "--since",
              "45m", "--tail", "1500", SERVICE)
lines.append("")
lines.append("## Last 45 minutes (up to 1500 lines)")
add("Log query succeeded", logs is not None)
log_lines = (logs or "").splitlines()
add("Log lines", len(log_lines))
patterns = {
    "Competing getUpdates poller / 409": r"code=409|409.*[Cc]onflict|terminated by other getUpdates request",
    "Telegram API 401": r"code=401|401.*[Uu]nauthorized",
    "Telegram API 403": r"code=403|403.*[Ff]orbidden",
    "Polling failures": r"Telegram long polling failed",
    "Network/dependency errors": r"RedisConnectionFailure|ConnectException|FeignException|Read timed out|Connection refused",
    "Startup events": r"Started TelegramBotServiceApplication|Starting TelegramBotServiceApplication",
    "ERROR-level or exceptions": r"\bERROR\b|\bException\b|OutOfMemoryError",
}
for category, pattern in patterns.items():
    matched = [row for row in log_lines if re.search(pattern, row, re.IGNORECASE)]
    add(category, len(matched))
    if matched:
        timestamps = [m.group(0)[:35]
                      for row in matched[-3:]
                      if (m := re.match(r"^\d{4}-\d\d-\d\dT[^ ]+", row))]
        if timestamps:
            add("Last occurrence UTC", ", ".join(timestamps))
containers = docker("ps", "--format", "{{.Names}}")
related = [name for name in (containers or "").splitlines()
           if "telegram-bot" in name.lower()]
lines.append("")
lines.append("## Local bot instances")
add("Matching running containers on Docker host", len(related))
for name in related[:8]:
    add("Container", re.sub(r"[^A-Za-z0-9_.-]", "", name)[:80])

# Inspect live JVM thread stacks without logging thread names, arguments, or payloads.
# Attach is read-only and does not restart or reconfigure the bot.
lines += ["", "## JVM long-polling thread inspection"]
if related:
    dump = docker("exec", related[0], "jcmd", "1", "Thread.print")
    add("jcmd thread dump available", dump is not None)
    if dump:
        sections = re.split(r'(?=^"[^"\n]+"\s+#\d+)', dump, flags=re.MULTILINE)
        add("JVM threads seen", sum(1 for section in sections
                                     if re.match(r'^"[^"\n]+"\s+#\d+', section)))
        relevant = []
        for section in sections:
            if not re.search(r"com\.pengrad\.telegrambot|okhttp3", section, re.IGNORECASE):
                continue
            state_match = re.search(r"java\.lang\.Thread\.State:\s*(\w+)", section)
            state = state_match.group(1) if state_match else "unknown"
            # Java class/method symbols only. NEVER emit raw thread titles or stack dump.
            frames = re.findall(r"^\s+at\s+([A-Za-z0-9_.$]+)\(", section, re.MULTILINE)
            relevant.append((state, frames))
        add("Threads with Telegram/OkHttp frames", len(relevant))
        for state, frames in relevant[:12]:
            add("Thread state", state)
            add("Top JVM frames", ", ".join(frames[:45])[:4800])
else:
    add("jcmd thread dump available", False)

history = docker("service", "logs", "--raw", "--timestamps", "--since",
                 "96h", "--tail", "5000", SERVICE)
lines += ["", "## Older service logs (last 96 hours, max 5000 lines)"]
add("History query succeeded", history is not None)
if history is not None:
    history_rows = history.splitlines()
    add("History log lines", len(history_rows))
    for category, pattern in patterns.items():
        matched = [row for row in history_rows if re.search(pattern, row, re.IGNORECASE)]
        add(category, len(matched))
        if category == "Polling failures":
            # Only numeric response codes, never copy Telegram error descriptions.
            counts = {}
            for row in matched:
                found = re.search(r"code=(\d{3})", row)
                if found:
                    code = found.group(1)
                    counts[code] = counts.get(code, 0) + 1
            for response_code, count in sorted(counts.items()):
                add(f"Polling response code {response_code}", count)
        if matched:
            last = re.match(r"^\d{4}-\d\d-\d\dT[^ ]+", matched[-1])
            if last:
                add("Last occurrence timestamp", last.group(0)[:35])

def telegram(method):
    if not token:
        return None, "token missing"
    try:
        req = urllib.request.Request(
            f"https://api.telegram.org/bot{token}/{method}",
            headers={"User-Agent": "Mnemos-readonly-diagnostic"},
        )
        with urllib.request.urlopen(req, timeout=12) as response:
            return json.load(response), None
    except urllib.error.HTTPError as exc:
        return None, f"HTTP {exc.code}"
    except (urllib.error.URLError, TimeoutError, ValueError):
        return None, "network/parse error"

lines.append("")
lines.append("## Telegram Bot API (read-only)")
webhook, error = telegram("getWebhookInfo")
add("getWebhookInfo succeeded", bool(webhook and webhook.get("ok")))
if webhook and webhook.get("ok"):
    result = webhook.get("result") or {}
    add("Webhook configured", bool(result.get("url")))
    add("Pending update count", result.get("pending_update_count", "unknown"))
    add("Webhook delivery error recorded", bool(result.get("last_error_date")))
elif error:
    add("Webhook API error", error)
me, error = telegram("getMe")
add("getMe succeeded", bool(me and me.get("ok")))
if error:
    add("getMe error", error)

lines += ["", "No getUpdates calls, webhook changes, restarts, or deployment actions were performed."]
report = "\n".join(lines)
print(report)
summary = os.environ.get("GITHUB_STEP_SUMMARY")
if summary:
    with open(summary, "a", encoding="utf-8") as handle:
        handle.write(report + "\n")

repository = os.environ["GITHUB_REPOSITORY"]
auth_token = os.environ["GH_TOKEN"]
payload = json.dumps({
    "title": "Telegram poller: read-only production diagnostic 2026-10-15",
    "body": report,
}).encode("utf-8")
req = urllib.request.Request(
    f"https://api.github.com/repos/{repository}/issues",
    data=payload,
    headers={
        "Authorization": f"Bearer {auth_token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "Content-Type": "application/json",
    },
    method="POST",
)
try:
    with urllib.request.urlopen(req, timeout=15) as response:
        issue = json.load(response)
    print("Diagnostic issue:", issue.get("html_url", "created"))
except urllib.error.HTTPError as exc:
    print(f"Failed to persist result to private repo issue: HTTP {exc.code}")
    raise SystemExit(1)
