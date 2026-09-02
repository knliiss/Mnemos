#!/usr/bin/env python3

from __future__ import annotations

import re
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
PRODUCTION_OVERRIDE = (
    REPOSITORY_ROOT / "infrastructure" / "production" / "compose.app-overrides.yml"
)

EXPECTED_HIKARI_MILLISECONDS = {
    "SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT": 5_000,
    "SPRING_DATASOURCE_HIKARI_VALIDATION_TIMEOUT": 3_000,
    "SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT": 60_000,
    "SPRING_DATASOURCE_HIKARI_MAX_LIFETIME": 1_800_000,
    "SPRING_DATASOURCE_HIKARI_KEEPALIVE_TIME": 300_000,
}


def fail(message: str) -> None:
    raise AssertionError(message)


def read_environment_values() -> dict[str, str]:
    content = PRODUCTION_OVERRIDE.read_text(encoding="utf-8")
    values: dict[str, str] = {}

    for key in EXPECTED_HIKARI_MILLISECONDS:
        pattern = rf"^[ ]{{2}}{re.escape(key)}:[ ]*\"([^\"]+)\"[ ]*$"
        matches = re.findall(pattern, content, flags=re.MULTILINE)

        if len(matches) != 1:
            fail(f"{key} must be declared exactly once in production datasource defaults")

        values[key] = matches[0]

    return values


def verify_millisecond_values(values: dict[str, str]) -> None:
    for key, expected_value in EXPECTED_HIKARI_MILLISECONDS.items():
        raw_value = values[key]

        if not raw_value.isdecimal():
            fail(
                f"{key} must use integer milliseconds because Hikari binds this property "
                f"as a long, got {raw_value!r}"
            )

        actual_value = int(raw_value)

        if actual_value != expected_value:
            fail(
                f"{key} must be {expected_value} milliseconds, got {actual_value}"
            )


def main() -> int:
    values = read_environment_values()
    verify_millisecond_values(values)

    print("Production Hikari timeout units verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
