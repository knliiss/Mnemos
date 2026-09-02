#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

STACK_NAME="${MNEMOS_SWARM_STACK_NAME:-mnemos}"
READINESS_URL="${MNEMOS_PRODUCTION_READINESS_URL:-https://api.knalis.rest/actuator/health/readiness}"
ATTEMPTS="${MNEMOS_AVAILABILITY_ATTEMPTS:-3}"
RETRY_SECONDS="${MNEMOS_AVAILABILITY_RETRY_SECONDS:-2}"

APPLICATION_SERVICES="
account-service
bet-service
event-service
gateway-service
moderation-service
notification-service
payment-service
personalization-service
player-directory-service
profile-service
realtime-service
reward-service
social-service
telegram-bot-service
wallet-service
"

case "$ATTEMPTS" in
    '' | *[!0-9]*)
        echo "MNEMOS_AVAILABILITY_ATTEMPTS must be a positive integer" >&2
        exit 1
        ;;
esac

case "$RETRY_SECONDS" in
    '' | *[!0-9]*)
        echo "MNEMOS_AVAILABILITY_RETRY_SECONDS must be a non-negative integer" >&2
        exit 1
        ;;
esac

if [ "$ATTEMPTS" -le 0 ]; then
    echo "MNEMOS_AVAILABILITY_ATTEMPTS must be greater than zero" >&2
    exit 1
fi

service_has_available_replica() {
    runtime_service="$1"
    replicas="$(
        docker service ls \
            --filter "name=$runtime_service" \
            --format '{{.Name}} {{.Replicas}}' \
            | awk -v expected="$runtime_service" '$1 == expected { print $2; exit }'
    )"

    actual="${replicas%%/*}"
    desired="${replicas##*/}"

    if [ -z "$actual" ] || [ -z "$desired" ]; then
        echo "Production service is missing: $runtime_service" >&2
        return 1
    fi

    if [ "$desired" -le 0 ] || [ "$actual" -le 0 ]; then
        echo "Production service has no available replicas: $runtime_service ($replicas)" >&2
        return 1
    fi

    return 0
}

verify_once() {
    failed=0

    for service_name in $APPLICATION_SERVICES; do
        if ! service_has_available_replica "${STACK_NAME}_${service_name}"; then
            failed=1
        fi
    done

    if ! service_has_available_replica "${STACK_NAME}_cloudflared"; then
        failed=1
    fi

    if [ "$failed" -ne 0 ]; then
        return 1
    fi

    response="$(
        curl \
            --fail-with-body \
            --silent \
            --show-error \
            --max-time 10 \
            "$READINESS_URL"
    )" || return 1

    if ! printf '%s' "$response" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"'; then
        echo "Public production readiness is not UP: $response" >&2
        return 1
    fi

    return 0
}

attempt=1

while [ "$attempt" -le "$ATTEMPTS" ]; do
    if verify_once; then
        echo "Production availability verified"
        exit 0
    fi

    if [ "$attempt" -lt "$ATTEMPTS" ]; then
        echo "Production availability attempt ${attempt}/${ATTEMPTS} failed; retrying" >&2
        sleep "$RETRY_SECONDS"
    fi

    attempt=$((attempt + 1))
done

echo "Production availability verification failed after ${ATTEMPTS} attempts" >&2
exit 1
