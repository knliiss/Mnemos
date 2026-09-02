#!/usr/bin/env python3

from __future__ import annotations

from typing import Any

import production_deployment_plan_base as base
from production_runtime_policy import deployment_priority


ProductionDeploymentPlanError = base.ProductionDeploymentPlanError
PLAN_SCHEMA_VERSION = base.PLAN_SCHEMA_VERSION
DEFAULT_STABLE_SECONDS = base.DEFAULT_STABLE_SECONDS
SUPPORTED_OPERATIONS = base.SUPPORTED_OPERATIONS


def action_order(
    component: str,
    catalog: dict[str, Any],
) -> tuple[int, int, str]:
    runtime_kind = catalog["services"][component]["deploy"]["runtime"]

    if runtime_kind == "compose":
        return (0, 0, component)

    return (1, deployment_priority(component), component)


base.action_order = action_order

build_plan = base.build_plan
validate_plan = base.validate_plan
read_plan = base.read_plan
write_plan = base.write_plan


def __getattr__(name: str) -> Any:
    return getattr(base, name)


def main() -> int:
    return base.main()


if __name__ == "__main__":
    raise SystemExit(main())
