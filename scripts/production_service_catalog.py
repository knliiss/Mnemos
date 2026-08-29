#!/usr/bin/env python3

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
CATALOG_PATH = REPOSITORY_ROOT / "infrastructure" / "production" / "service-catalog.json"


class ProductionServiceCatalogError(RuntimeError):
    pass


def load_catalog() -> dict[str, Any]:
    try:
        catalog = json.loads(CATALOG_PATH.read_text(encoding="utf-8"))
    except FileNotFoundError as exception:
        raise ProductionServiceCatalogError(
            f"Production service catalog is missing: {CATALOG_PATH}"
        ) from exception
    except json.JSONDecodeError as exception:
        raise ProductionServiceCatalogError(
            f"Production service catalog is invalid JSON: {exception}"
        ) from exception

    if not isinstance(catalog, dict):
        raise ProductionServiceCatalogError("Production service catalog must be an object")

    if catalog.get("schema_version") != 1:
        raise ProductionServiceCatalogError("Unsupported production service catalog schema")

    services = catalog.get("services")

    if not isinstance(services, dict) or not services:
        raise ProductionServiceCatalogError(
            "Production service catalog must contain a non-empty services object"
        )

    return catalog


def services() -> dict[str, dict[str, Any]]:
    raw_services = load_catalog()["services"]

    return {
        str(name): service
        for name, service in raw_services.items()
        if isinstance(service, dict)
    }


def service(name: str) -> dict[str, Any]:
    configured = services()
    result = configured.get(name)

    if result is None:
        available = ", ".join(sorted(configured))
        raise ProductionServiceCatalogError(
            f"Unknown production service '{name}'. Available services: {available}"
        )

    return result


def list_services(kind: str | None = None) -> list[str]:
    configured = services()

    if kind is None:
        return sorted(configured)

    return sorted(
        name
        for name, specification in configured.items()
        if specification.get("kind") == kind
    )


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Inspect the Mnemos production service catalog.",
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    list_parser = subparsers.add_parser("list")
    list_parser.add_argument(
        "--kind",
        choices=("application", "infrastructure"),
    )

    show_parser = subparsers.add_parser("show")
    show_parser.add_argument("service")

    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()

    try:
        if arguments.command == "list":
            for name in list_services(arguments.kind):
                print(name)

            return 0

        if arguments.command == "show":
            print(json.dumps(service(arguments.service), indent=2, sort_keys=False))
            return 0
    except ProductionServiceCatalogError as exception:
        print(str(exception), file=sys.stderr)
        return 1

    return 1


if __name__ == "__main__":
    raise SystemExit(main())
