#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="${1:-}"
OVERRIDE_FILE="${2:-}"
PROBE_SCHEMA="account"
PROBE_TABLE="security_migration_probe"

fail() {
    echo "$1" >&2
    exit 1
}

if [ -z "$ENV_FILE" ]; then
    echo "Usage: $0 <env-file> [compose-override-file]" >&2
    exit 2
fi

if [ ! -f "$ENV_FILE" ]; then
    fail "Environment file does not exist: $ENV_FILE"
fi

read_env() {
    key="$1"

    awk -F= -v key="$key" '
        $1 == key {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$ENV_FILE"
}

compose() {
    compose_args=""

    for file in \
        "$REPOSITORY_ROOT"/infrastructure/compose/compose.*.yml \
        "$REPOSITORY_ROOT"/infrastructure/compose/apps/compose.*.yml; do
        compose_args="$compose_args -f $file"
    done

    if [ -n "$OVERRIDE_FILE" ]; then
        compose_args="$compose_args -f $OVERRIDE_FILE"
    fi

    # Repository compose paths do not contain whitespace.
    # shellcheck disable=SC2086
    sh "$SCRIPT_DIR/docker-compose-env.sh" "$ENV_FILE" $compose_args "$@"
}

query_scalar() {
    query="$1"

    compose exec -T app-postgres \
        psql \
        --username "$ADMIN_USER" \
        --dbname "$DATABASE" \
        --tuples-only \
        --no-align \
        --command "$query" |
        tr -d '[:space:]'
}

cleanup_probe() {
    compose exec -T app-postgres \
        psql \
        --username "$ADMIN_USER" \
        --dbname "$DATABASE" \
        --command "DROP TABLE IF EXISTS ${PROBE_SCHEMA}.${PROBE_TABLE};" >/dev/null 2>&1 || true
}

ADMIN_USER="$(read_env APP_POSTGRES_USER)"
DATABASE="$(read_env APP_POSTGRES_DB)"
ACCOUNT_ROLE="$(read_env ACCOUNT_POSTGRES_USER)"

[ -n "$ADMIN_USER" ] || fail "APP_POSTGRES_USER is missing"
[ -n "$DATABASE" ] || fail "APP_POSTGRES_DB is missing"
[ -n "$ACCOUNT_ROLE" ] || fail "ACCOUNT_POSTGRES_USER is missing"

trap cleanup_probe EXIT INT TERM

cleanup_probe

compose exec -T app-postgres \
    psql \
    --username "$ADMIN_USER" \
    --dbname "$DATABASE" \
    --set ON_ERROR_STOP=1 \
    --command "CREATE TABLE ${PROBE_SCHEMA}.${PROBE_TABLE} (id bigint PRIMARY KEY);" >/dev/null

owner_before="$(query_scalar "
    SELECT owner.rolname
    FROM pg_class relation
    JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
    JOIN pg_roles owner ON owner.oid = relation.relowner
    WHERE namespace.nspname = '${PROBE_SCHEMA}'
      AND relation.relname = '${PROBE_TABLE}';
")"

if [ "$owner_before" != "$ADMIN_USER" ]; then
    fail "Legacy ownership probe was not created with the admin owner"
fi

compose start app-postgres-security-init >/dev/null

bootstrap_container="$(compose ps -aq app-postgres-security-init)"

if [ -z "$bootstrap_container" ]; then
    fail "PostgreSQL security bootstrap container is missing"
fi

bootstrap_exit_code="$(docker wait "$bootstrap_container")"

if [ "$bootstrap_exit_code" != "0" ]; then
    compose logs --no-color --tail=200 app-postgres-security-init >&2 || true
    fail "PostgreSQL security bootstrap failed while migrating legacy ownership"
fi

owner_after="$(query_scalar "
    SELECT owner.rolname
    FROM pg_class relation
    JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
    JOIN pg_roles owner ON owner.oid = relation.relowner
    WHERE namespace.nspname = '${PROBE_SCHEMA}'
      AND relation.relname = '${PROBE_TABLE}';
")"

if [ "$owner_after" != "$ACCOUNT_ROLE" ]; then
    fail "Legacy PostgreSQL ownership was not transferred to the account service role"
fi

echo "PostgreSQL legacy ownership migration verified"
