#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
VALIDATOR="$SCRIPT_DIR/validate-compose-secrets.sh"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT INT TERM

VALID_ENV="$TEMP_DIR/valid.env"
PLACEHOLDER_ENV="$TEMP_DIR/placeholder.env"
MISSING_ENV="$TEMP_DIR/missing.env"
SHORT_ENV="$TEMP_DIR/short.env"
DUPLICATE_ENV="$TEMP_DIR/duplicate.env"

for key in $(sh "$VALIDATOR" --list-required); do
    printf '%s=%s\n' "$key" "secure-${key}-value-2026" >> "$VALID_ENV"
done

sh "$VALIDATOR" "$VALID_ENV" >/dev/null

awk '
    /^REDIS_ADMIN_PASSWORD=/ {
        print "REDIS_ADMIN_PASSWORD=change_me"
        next
    }
    { print }
' "$VALID_ENV" > "$PLACEHOLDER_ENV"

if sh "$VALIDATOR" "$PLACEHOLDER_ENV" >/dev/null 2>&1; then
    echo "Compose secret validation accepted a placeholder value" >&2
    exit 1
fi

awk '
    !/^GATEWAY_MODERATION_API_KEY=/ {
        print
    }
' "$VALID_ENV" > "$MISSING_ENV"

if sh "$VALIDATOR" "$MISSING_ENV" >/dev/null 2>&1; then
    echo "Compose secret validation accepted a missing required secret" >&2
    exit 1
fi

awk '
    /^APP_POSTGRES_PASSWORD=/ {
        print "APP_POSTGRES_PASSWORD=too-short"
        next
    }
    { print }
' "$VALID_ENV" > "$SHORT_ENV"

if sh "$VALIDATOR" "$SHORT_ENV" >/dev/null 2>&1; then
    echo "Compose secret validation accepted a short secret" >&2
    exit 1
fi

awk '
    /^KAFKA_ADMIN_PASSWORD=/ {
        print "KAFKA_ADMIN_PASSWORD=secure-KAFKA_BROKER_PASSWORD-value-2026"
        next
    }
    { print }
' "$VALID_ENV" > "$DUPLICATE_ENV"

if sh "$VALIDATOR" "$DUPLICATE_ENV" >/dev/null 2>&1; then
    echo "Compose secret validation accepted a reused secret value" >&2
    exit 1
fi

echo "Compose secret validation verified"
