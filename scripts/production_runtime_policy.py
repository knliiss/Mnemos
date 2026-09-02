#!/usr/bin/env python3

from __future__ import annotations


APPLICATION_SERVICES = (
    "account-service",
    "bet-service",
    "event-service",
    "gateway-service",
    "moderation-service",
    "notification-service",
    "payment-service",
    "personalization-service",
    "player-directory-service",
    "profile-service",
    "realtime-service",
    "reward-service",
    "social-service",
    "telegram-bot-service",
    "wallet-service",
)

ACTIVE_ACTIVE_SERVICES = frozenset(
    {
        "account-service",
        "event-service",
        "gateway-service",
        "moderation-service",
        "payment-service",
        "personalization-service",
        "player-directory-service",
        "wallet-service",
    }
)

SINGLETON_SERVICES = frozenset(APPLICATION_SERVICES) - ACTIVE_ACTIVE_SERVICES

DEPLOYMENT_PRIORITIES = {
    "account-service": 10,
    "event-service": 20,
    "payment-service": 20,
    "wallet-service": 20,
    "moderation-service": 30,
    "personalization-service": 30,
    "player-directory-service": 30,
    "profile-service": 35,
    "social-service": 35,
    "bet-service": 40,
    "notification-service": 40,
    "reward-service": 40,
    "realtime-service": 50,
    "gateway-service": 90,
    "telegram-bot-service": 100,
}


def validate_policy() -> None:
    application_services = set(APPLICATION_SERVICES)

    if ACTIVE_ACTIVE_SERVICES & SINGLETON_SERVICES:
        raise ValueError("Production runtime policy contains overlapping service classes")

    if ACTIVE_ACTIVE_SERVICES | SINGLETON_SERVICES != application_services:
        raise ValueError("Production runtime policy does not classify every application service")

    if set(DEPLOYMENT_PRIORITIES) != application_services:
        raise ValueError("Production deployment priorities do not cover every application service")


def require_application_service(service_name: str) -> None:
    if service_name not in APPLICATION_SERVICES:
        raise ValueError(f"Unknown production application service: {service_name}")


def desired_replicas(service_name: str) -> int:
    require_application_service(service_name)
    return 2 if service_name in ACTIVE_ACTIVE_SERVICES else 1


def update_order(service_name: str) -> str:
    require_application_service(service_name)
    return "start-first" if service_name in ACTIVE_ACTIVE_SERVICES else "stop-first"


def deployment_priority(service_name: str) -> int:
    require_application_service(service_name)
    return DEPLOYMENT_PRIORITIES[service_name]


validate_policy()
