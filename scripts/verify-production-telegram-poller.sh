#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

SERVICE_NAME="${MNEMOS_TELEGRAM_BOT_SWARM_SERVICE:-mnemos_telegram-bot-service}"
LOG_WINDOW="${MNEMOS_TELEGRAM_POLLING_LOG_WINDOW:-5m}"

replicas="$(
    docker service ls \
        --filter "name=$SERVICE_NAME" \
        --format '{{.Name}} {{.Replicas}}' \
        | awk -v expected="$SERVICE_NAME" '$1 == expected { print $2; exit }'
)"

if [ "$replicas" != "1/1" ]; then
    echo "Telegram bot production poller must be exactly 1/1: $SERVICE_NAME ($replicas)" >&2
    exit 1
fi

recent_logs="$(
    docker service logs \
        --raw \
        --timestamps \
        --since "$LOG_WINDOW" \
        --tail 1000 \
        "$SERVICE_NAME" 2>&1 || true
)"

if printf '%s\n' "$recent_logs" \
    | grep -Eq 'code=409|409.*Conflict|terminated by other getUpdates request'; then
    echo "Telegram bot production poller has a recent getUpdates conflict" >&2
    printf '%s\n' "$recent_logs" \
        | grep -E 'code=409|409.*Conflict|terminated by other getUpdates request' \
        | tail -20 >&2
    exit 1
fi

echo "Telegram production poller singleton verified"
