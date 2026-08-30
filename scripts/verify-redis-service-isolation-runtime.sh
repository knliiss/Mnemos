#!/usr/bin/env sh
set -eu

TARGET_ENV="${1:-.env}"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Target env file does not exist: $TARGET_ENV" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required" >&2
    exit 1
fi

get_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$TARGET_ENV"
}

require_value() {
    name="$1"
    value="$2"

    if [ -z "$value" ]; then
        echo "$name is required" >&2
        exit 1
    fi
}

project_name="$(get_env COMPOSE_PROJECT_NAME)"
admin_password="$(get_env REDIS_ADMIN_PASSWORD)"
gateway_password="$(get_env REDIS_GATEWAY_PASSWORD)"
realtime_password="$(get_env REDIS_REALTIME_PASSWORD)"
notification_password="$(get_env REDIS_NOTIFICATION_PASSWORD)"
telegram_bot_password="$(get_env REDIS_TELEGRAM_BOT_PASSWORD)"

require_value "COMPOSE_PROJECT_NAME" "$project_name"
require_value "REDIS_ADMIN_PASSWORD" "$admin_password"
require_value "REDIS_GATEWAY_PASSWORD" "$gateway_password"
require_value "REDIS_REALTIME_PASSWORD" "$realtime_password"
require_value "REDIS_NOTIFICATION_PASSWORD" "$notification_password"
require_value "REDIS_TELEGRAM_BOT_PASSWORD" "$telegram_bot_password"

redis_container_id="$(
    docker ps -aq \
        --filter "label=com.docker.compose.project=$project_name" \
        --filter "label=com.docker.compose.service=redis" |
        head -n 1
)"

require_value "Redis container id" "$redis_container_id"

redis_state="$(docker inspect --format '{{.State.Status}}' "$redis_container_id")"

if [ "$redis_state" != "running" ]; then
    echo "Redis must be running for service isolation verification" >&2
    exit 1
fi

run_redis() {
    user="$1"
    password="$2"
    shift 2

    docker exec \
        -e "REDISCLI_AUTH=$password" \
        "$redis_container_id" \
        redis-cli \
        --raw \
        --user "$user" \
        "$@" 2>&1
}

expect_exact() {
    expected="$1"
    actual="$2"
    context="$3"

    if [ "$actual" != "$expected" ]; then
        echo "$context: expected '$expected', got '$actual'" >&2
        exit 1
    fi
}

expect_noperm() {
    actual="$1"
    context="$2"

    case "$actual" in
        *NOPERM*)
            ;;
        *)
            echo "$context: expected NOPERM, got '$actual'" >&2
            exit 1
            ;;
    esac
}

gateway_key="mnemos:gateway:security-isolation-probe"
realtime_key="realtime:security-isolation-probe"
notification_key="notification:security-isolation-probe"
telegram_bot_key="telegram-bot:security-isolation-probe"

cleanup() {
    run_redis redis_admin "$admin_password" DEL \
        "$gateway_key" \
        "$realtime_key" \
        "$notification_key" \
        "$telegram_bot_key" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM

unauthenticated_ping="$(
    docker exec "$redis_container_id" redis-cli --raw ping 2>&1 || true
)"

case "$unauthenticated_ping" in
    *NOAUTH*)
        ;;
    *)
        echo "Redis default user is unexpectedly usable: '$unauthenticated_ping'" >&2
        exit 1
        ;;
esac

expect_exact \
    "OK" \
    "$(run_redis gateway_service "$gateway_password" SET "$gateway_key" gateway)" \
    "Gateway Redis namespace write"

expect_exact \
    "OK" \
    "$(run_redis realtime_service "$realtime_password" SET "$realtime_key" realtime)" \
    "Realtime Redis namespace write"

expect_exact \
    "OK" \
    "$(run_redis notification_service "$notification_password" SET "$notification_key" notification)" \
    "Notification Redis namespace write"

expect_exact \
    "OK" \
    "$(run_redis telegram_bot_service "$telegram_bot_password" SET "$telegram_bot_key" menu)" \
    "Telegram bot Redis namespace write"

expect_exact \
    "OK" \
    "$(run_redis gateway_service "$gateway_password" EVAL "return redis.call('SET', KEYS[1], ARGV[1])" 1 "$gateway_key" scripted)" \
    "Gateway Redis scripting"

expect_exact \
    "OK" \
    "$(run_redis realtime_service "$realtime_password" EVAL "local now = redis.call('TIME'); return redis.call('SET', KEYS[1], now[1])" 1 "$realtime_key")" \
    "Realtime Redis scripting"

expect_exact \
    "OK" \
    "$(run_redis notification_service "$notification_password" EVAL "return redis.call('SET', KEYS[1], ARGV[1])" 1 "$notification_key" scripted)" \
    "Notification Redis scripting"

expect_noperm \
    "$(run_redis telegram_bot_service "$telegram_bot_password" EVAL "return 1" 0 || true)" \
    "Telegram bot Redis scripting"

expect_noperm \
    "$(run_redis gateway_service "$gateway_password" GET "$realtime_key" || true)" \
    "Gateway reading realtime Redis state"

expect_noperm \
    "$(run_redis gateway_service "$gateway_password" SET "$notification_key" compromised || true)" \
    "Gateway writing notification Redis state"

expect_noperm \
    "$(run_redis realtime_service "$realtime_password" GET "$notification_key" || true)" \
    "Realtime reading notification Redis state"

expect_noperm \
    "$(run_redis notification_service "$notification_password" GET "$gateway_key" || true)" \
    "Notification reading gateway Redis state"

expect_noperm \
    "$(run_redis telegram_bot_service "$telegram_bot_password" GET "$notification_key" || true)" \
    "Telegram bot reading notification Redis state"

expect_noperm \
    "$(run_redis notification_service "$notification_password" GET "$telegram_bot_key" || true)" \
    "Notification reading Telegram bot Redis state"

expect_noperm \
    "$(run_redis gateway_service "$gateway_password" FLUSHALL || true)" \
    "Gateway dangerous Redis command"

expect_noperm \
    "$(run_redis realtime_service "$realtime_password" ACL WHOAMI || true)" \
    "Realtime Redis ACL administration"

expect_noperm \
    "$(run_redis notification_service "$notification_password" CONFIG GET '*' || true)" \
    "Notification Redis configuration access"

expect_noperm \
    "$(run_redis telegram_bot_service "$telegram_bot_password" FLUSHDB || true)" \
    "Telegram bot dangerous Redis command"

expect_exact \
    "PONG" \
    "$(run_redis redis_admin "$admin_password" PING)" \
    "Redis admin health access"

echo "Redis service key and command isolation verified"
