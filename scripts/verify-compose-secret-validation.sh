#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
VALIDATOR="$SCRIPT_DIR/validate-compose-secrets.sh"
EXAMPLE_ENV="$REPOSITORY_ROOT/.env.example"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT INT TERM

VALID_ENV="$TEMP_DIR/valid.env"
PLACEHOLDER_ENV="$TEMP_DIR/placeholder.env"
MISSING_ENV="$TEMP_DIR/missing.env"
SHORT_ENV="$TEMP_DIR/short.env"
DUPLICATE_ENV="$TEMP_DIR/duplicate.env"
DUPLICATE_KEY_ENV="$TEMP_DIR/duplicate-key.env"
REQUIRED_KEYS="$TEMP_DIR/required-keys"
GENERATED_KEYS="$TEMP_DIR/generated-keys"
COMPOSE_SECRET_KEYS="$TEMP_DIR/compose-secret-keys"

sh "$VALIDATOR" --list-required > "$REQUIRED_KEYS"
sh "$VALIDATOR" --list-generated > "$GENERATED_KEYS"

while IFS= read -r key; do
    if ! grep -Fqx -e "$key" "$REQUIRED_KEYS"; then
        echo "Generated secret is not part of the required secret catalog: $key" >&2
        exit 1
    fi
done < "$GENERATED_KEYS"

while IFS= read -r key; do
    if ! grep -Fqx -e "$key" "$REQUIRED_KEYS"; then
        echo "Secret-like .env.example value is missing from compose validation: $key" >&2
        exit 1
    fi
done <<EOF
$(awk -F= '
    $1 ~ /(_PASSWORD|_API_KEY|_TOKEN|_CLIENT_SECRET|_SECRET_KEY|_PROJECT_KEY)$/ {
        print $1
    }
' "$EXAMPLE_ENV")
EOF

while IFS= read -r key; do
    if ! grep -q "^${key}=" "$EXAMPLE_ENV"; then
        echo "Required compose secret is missing from .env.example: $key" >&2
        exit 1
    fi
done < "$REQUIRED_KEYS"

awk '
    {
        remaining = $0

        while (match(remaining, /\$\{[A-Z][A-Z0-9_]*[^}]*\}/)) {
            expression = substr(remaining, RSTART + 2, RLENGTH - 3)
            key = expression
            sub(/[:?+-].*$/, "", key)

            if (key ~ /(_PASSWORD|_API_KEY|_TOKEN|_CLIENT_SECRET|_SECRET_KEY|_PRIVATE_KEY|_PROJECT_KEY)$/) {
                print key
            }

            remaining = substr(remaining, RSTART + RLENGTH)
        }
    }
' \
    "$REPOSITORY_ROOT"/infrastructure/compose/*.yml \
    "$REPOSITORY_ROOT"/infrastructure/compose/apps/*.yml \
    "$REPOSITORY_ROOT"/infrastructure/production/*.yml |
    sort -u > "$COMPOSE_SECRET_KEYS"

while IFS= read -r key; do
    case "$key" in
        *_KAFKA_SIGNING_PRIVATE_KEY)
            # Kafka signing keys are dynamically discovered, generated and
            # validated by generate/verify-kafka-message-auth.sh.
            continue
            ;;
    esac

    if ! grep -Fqx -e "$key" "$REQUIRED_KEYS"; then
        echo "Secret-like Compose variable is missing from compose validation: $key" >&2
        exit 1
    fi
done < "$COMPOSE_SECRET_KEYS"

for key in $(cat "$REQUIRED_KEYS"); do
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

cp "$VALID_ENV" "$DUPLICATE_KEY_ENV"
printf '%s\n' 'REDIS_ADMIN_PASSWORD=another-secure-redis-admin-value-2026' >> "$DUPLICATE_KEY_ENV"

if sh "$VALIDATOR" "$DUPLICATE_KEY_ENV" >/dev/null 2>&1; then
    echo "Compose secret validation accepted a duplicate required variable" >&2
    exit 1
fi

echo "Compose secret validation verified"
