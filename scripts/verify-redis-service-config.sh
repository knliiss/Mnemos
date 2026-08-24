#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
REDIS_COMPOSE="$REPOSITORY_ROOT/infrastructure/compose/compose.redis.yml"
REDIS_ENTRYPOINT="$REPOSITORY_ROOT/infrastructure/redis/redis-entrypoint.sh"
MAKE_CONFIG="$REPOSITORY_ROOT/make/config.mk"

fail() {
    echo "$1" >&2
    exit 1
}

[ -f "$REDIS_COMPOSE" ] || fail "Redis Compose file is missing"
[ -f "$REDIS_ENTRYPOINT" ] || fail "Redis ACL entrypoint is missing"
[ -f "$MAKE_CONFIG" ] || fail "Make configuration is missing"

grep -Eq '^BUILDABLE_INFRA_SERVICE_NAMES[[:space:]]*:=[[:space:]].*(^|[[:space:]])redis([[:space:]]|$)' "$MAKE_CONFIG" ||
    fail "Hardened Redis image must be included in the project image build"

grep -Fq -- '--aclfile' "$REDIS_COMPOSE" ||
    fail "Redis must load an ACL file"

if grep -Fq -- '--requirepass' "$REDIS_COMPOSE"; then
    fail "Redis must not use one shared requirepass credential"
fi

grep -Fq 'REDIS_ADMIN_PASSWORD: ${REDIS_ADMIN_PASSWORD}' "$REDIS_COMPOSE" ||
    fail "Redis container does not receive its admin credential"

grep -Fq 'user default off' "$REDIS_ENTRYPOINT" ||
    fail "Redis default user must be disabled"

grep -Fq 'user redis_admin on #' "$REDIS_ENTRYPOINT" ||
    fail "Redis admin ACL user is missing"

verify_service() {
    service="$1"
    user="$2"
    password_variable="$3"
    namespace="$4"
    application_config="$5"

    compose_file="$REPOSITORY_ROOT/infrastructure/compose/apps/compose.$service.yml"

    [ -f "$compose_file" ] || fail "$service Compose file is missing"
    [ -f "$application_config" ] || fail "$service application configuration is missing"

    grep -Fq "SPRING_DATA_REDIS_USERNAME: $user" "$compose_file" ||
        fail "$service does not use its Redis ACL username"

    grep -Fq "SPRING_DATA_REDIS_PASSWORD: \${$password_variable}" "$compose_file" ||
        fail "$service does not use its dedicated Redis password"

    grep -Fq 'SPRING_DATA_REDIS_USERNAME' "$application_config" ||
        fail "$service application configuration does not support Redis usernames"

    acl_line="$(grep -F "user $user " "$REDIS_ENTRYPOINT" || true)"

    [ -n "$acl_line" ] || fail "Redis ACL user is missing for $service"

    printf '%s\n' "$acl_line" | grep -Fq "~$namespace" ||
        fail "$service Redis ACL does not restrict keys to $namespace"

    printf '%s\n' "$acl_line" | grep -Fq -- '-@all' ||
        fail "$service Redis ACL must start from no command permissions"

    if printf '%s\n' "$acl_line" | grep -Fq -- '+@all'; then
        fail "$service Redis ACL must not grant all commands"
    fi

    for blocked_command in flushall flushdb swapdb function script; do
        printf '%s\n' "$acl_line" | grep -Fq -- "-$blocked_command" ||
            fail "$service Redis ACL does not block $blocked_command"
    done

    grep -Fq "$password_variable: \${$password_variable}" "$REDIS_COMPOSE" ||
        fail "Redis container does not receive $password_variable"
}

verify_service \
    "gateway-service" \
    "gateway_service" \
    "REDIS_GATEWAY_PASSWORD" \
    "mnemos:gateway:*" \
    "$REPOSITORY_ROOT/apps/gateway-service/src/main/resources/application.yml"

verify_service \
    "realtime-service" \
    "realtime_service" \
    "REDIS_REALTIME_PASSWORD" \
    "realtime:*" \
    "$REPOSITORY_ROOT/apps/realtime-service/src/main/resources/application.properties"

verify_service \
    "notification-service" \
    "notification_service" \
    "REDIS_NOTIFICATION_PASSWORD" \
    "notification:*" \
    "$REPOSITORY_ROOT/apps/notification-service/src/main/resources/application.properties"

if grep -R -n -E '(^|[^A-Z0-9_])REDIS_PASSWORD([^A-Z0-9_]|$)' \
    "$REPOSITORY_ROOT/infrastructure/compose" \
    "$REPOSITORY_ROOT/apps/gateway-service/src/main/resources" \
    "$REPOSITORY_ROOT/apps/realtime-service/src/main/resources" \
    "$REPOSITORY_ROOT/apps/notification-service/src/main/resources" >/dev/null 2>&1; then
    fail "Shared REDIS_PASSWORD wiring must not remain in application or Compose configuration"
fi

echo "Redis service ACL configuration verified"
