#!/usr/bin/env sh
set -eu

SEEN_SERVICE_ROLES=""
SEEN_SERVICE_PASSWORDS=""

require_value() {
    name="$1"
    value="$2"

    if [ -z "$value" ]; then
        echo "Required database access value is missing: $name" >&2
        exit 1
    fi
}

validate_service_identity() {
    service_schema="$1"
    service_role="$2"
    service_password="$3"

    if ! printf '%s' "$service_role" | grep -Eq '^[a-z_][a-z0-9_]*$'; then
        echo "Invalid PostgreSQL role name for $service_schema: $service_role" >&2
        exit 1
    fi

    if [ "$service_role" = "$PGUSER" ]; then
        echo "Service PostgreSQL role must not reuse the admin role: $service_schema" >&2
        exit 1
    fi

    if printf '%b' "$SEEN_SERVICE_ROLES" | grep -Fqx -e "$service_role"; then
        echo "Service PostgreSQL roles must be unique: $service_role" >&2
        exit 1
    fi

    if [ "$service_password" = "$PGPASSWORD" ]; then
        echo "Service PostgreSQL password must not reuse the admin password: $service_schema" >&2
        exit 1
    fi

    if printf '%b' "$SEEN_SERVICE_PASSWORDS" | grep -Fqx -e "$service_password"; then
        echo "Service PostgreSQL passwords must be unique: $service_schema" >&2
        exit 1
    fi

    SEEN_SERVICE_ROLES="${SEEN_SERVICE_ROLES}${service_role}\n"
    SEEN_SERVICE_PASSWORDS="${SEEN_SERVICE_PASSWORDS}${service_password}\n"
}

configure_service_role() {
    service_schema="$1"
    service_role="$2"
    service_password="$3"

    require_value "${service_schema}_role" "$service_role"
    require_value "${service_schema}_password" "$service_password"
    validate_service_identity "$service_schema" "$service_role" "$service_password"

    psql \
        --set ON_ERROR_STOP=1 \
        --set service_schema="$service_schema" \
        --set service_role="$service_role" \
        --set service_password="$service_password" <<'SQL'
SELECT format(
    'CREATE ROLE %I LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS',
    :'service_role',
    :'service_password'
)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_roles
    WHERE rolname = :'service_role'
)
\gexec

SELECT format(
    'ALTER ROLE %I WITH LOGIN PASSWORD %L NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS',
    :'service_role',
    :'service_password'
)
\gexec

SELECT format(
    'CREATE SCHEMA %I AUTHORIZATION %I',
    :'service_schema',
    :'service_role'
)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_namespace
    WHERE nspname = :'service_schema'
)
\gexec

SELECT format(
    'ALTER TABLE %I.%I OWNER TO %I',
    namespace.nspname,
    relation.relname,
    :'service_role'
)
FROM pg_class relation
JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
WHERE namespace.nspname = :'service_schema'
  AND relation.relkind IN ('r', 'p')
\gexec

SELECT format(
    'ALTER SEQUENCE %I.%I OWNER TO %I',
    namespace.nspname,
    relation.relname,
    :'service_role'
)
FROM pg_class relation
JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
WHERE namespace.nspname = :'service_schema'
  AND relation.relkind = 'S'
\gexec

SELECT format(
    'ALTER VIEW %I.%I OWNER TO %I',
    namespace.nspname,
    relation.relname,
    :'service_role'
)
FROM pg_class relation
JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
WHERE namespace.nspname = :'service_schema'
  AND relation.relkind = 'v'
\gexec

SELECT format(
    'ALTER MATERIALIZED VIEW %I.%I OWNER TO %I',
    namespace.nspname,
    relation.relname,
    :'service_role'
)
FROM pg_class relation
JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
WHERE namespace.nspname = :'service_schema'
  AND relation.relkind = 'm'
\gexec

SELECT format(
    'ALTER FOREIGN TABLE %I.%I OWNER TO %I',
    namespace.nspname,
    relation.relname,
    :'service_role'
)
FROM pg_class relation
JOIN pg_namespace namespace ON namespace.oid = relation.relnamespace
WHERE namespace.nspname = :'service_schema'
  AND relation.relkind = 'f'
\gexec

SELECT format(
    'ALTER %s %I.%I(%s) OWNER TO %I',
    CASE routine.prokind
        WHEN 'p' THEN 'PROCEDURE'
        WHEN 'a' THEN 'AGGREGATE'
        ELSE 'FUNCTION'
    END,
    namespace.nspname,
    routine.proname,
    pg_get_function_identity_arguments(routine.oid),
    :'service_role'
)
FROM pg_proc routine
JOIN pg_namespace namespace ON namespace.oid = routine.pronamespace
WHERE namespace.nspname = :'service_schema'
\gexec

SELECT format(
    'ALTER TYPE %I.%I OWNER TO %I',
    namespace.nspname,
    type.typname,
    :'service_role'
)
FROM pg_type type
JOIN pg_namespace namespace ON namespace.oid = type.typnamespace
WHERE namespace.nspname = :'service_schema'
  AND (
      type.typtype IN ('d', 'e', 'r', 'm')
      OR (type.typtype = 'c' AND type.typrelid = 0)
  )
\gexec

SELECT format(
    'ALTER SCHEMA %I OWNER TO %I',
    :'service_schema',
    :'service_role'
)
\gexec

SELECT format(
    'REVOKE ALL ON SCHEMA %I FROM PUBLIC',
    :'service_schema'
)
\gexec
SQL
}

require_value "PGDATABASE" "${PGDATABASE:-}"
require_value "PGUSER" "${PGUSER:-}"
require_value "PGPASSWORD" "${PGPASSWORD:-}"

psql --set ON_ERROR_STOP=1 <<'SQL'
SELECT format(
    'REVOKE CREATE ON DATABASE %I FROM PUBLIC',
    current_database()
)
\gexec

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
SQL

configure_service_role "account" "$ACCOUNT_POSTGRES_USER" "$ACCOUNT_POSTGRES_PASSWORD"
configure_service_role "wallet" "$WALLET_POSTGRES_USER" "$WALLET_POSTGRES_PASSWORD"
configure_service_role "event" "$EVENT_POSTGRES_USER" "$EVENT_POSTGRES_PASSWORD"
configure_service_role "notification" "$NOTIFICATION_POSTGRES_USER" "$NOTIFICATION_POSTGRES_PASSWORD"
configure_service_role "reward" "$REWARD_POSTGRES_USER" "$REWARD_POSTGRES_PASSWORD"
configure_service_role "bet" "$BET_POSTGRES_USER" "$BET_POSTGRES_PASSWORD"
configure_service_role "moderation" "$MODERATION_POSTGRES_USER" "$MODERATION_POSTGRES_PASSWORD"
configure_service_role "realtime" "$REALTIME_POSTGRES_USER" "$REALTIME_POSTGRES_PASSWORD"
configure_service_role "personalization" "$PERSONALIZATION_POSTGRES_USER" "$PERSONALIZATION_POSTGRES_PASSWORD"
configure_service_role "social" "$SOCIAL_POSTGRES_USER" "$SOCIAL_POSTGRES_PASSWORD"
configure_service_role "profile" "$PROFILE_POSTGRES_USER" "$PROFILE_POSTGRES_PASSWORD"
configure_service_role "player_directory" "$PLAYER_DIRECTORY_POSTGRES_USER" "$PLAYER_DIRECTORY_POSTGRES_PASSWORD"

echo "Application PostgreSQL service roles and schema ownership are configured."
