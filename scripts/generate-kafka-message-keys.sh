#!/usr/bin/env sh
set -eu

TARGET_ENV="${1:-.env}"

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Target env file does not exist: $TARGET_ENV" >&2
    exit 1
fi

if ! command -v openssl >/dev/null 2>&1; then
    echo "openssl is required" >&2
    exit 1
fi

TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT INT TERM

get_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$TARGET_ENV"
}

set_env() {
    key="$1"
    value="$2"
    temp_file="$TEMP_DIR/env"

    awk -v key="$key" -v value="$value" '
        BEGIN { found = 0 }
        index($0, key "=") == 1 {
            if (!found) {
                print key "=" value
                found = 1
            }
            next
        }
        { print }
        END {
            if (!found) {
                print key "=" value
            }
        }
    ' "$TARGET_ENV" > "$temp_file"

    cat "$temp_file" > "$TARGET_ENV"
}

remove_env() {
    key="$1"
    temp_file="$TEMP_DIR/env"

    awk -v key="$key" 'index($0, key "=") != 1 { print }' "$TARGET_ENV" > "$temp_file"
    cat "$temp_file" > "$TARGET_ENV"
}

service_variable_name() {
    printf '%s' "$1" | tr '[:lower:]-' '[:upper:]_'
}

discover_services() {
    for compose_file in "$REPOSITORY_ROOT"/infrastructure/compose/apps/compose.*-service.yml; do
        [ -f "$compose_file" ] || continue

        if grep -q 'MNEMOS_KAFKA_MESSAGEAUTH_PRIVATEKEY:' "$compose_file"; then
            basename "$compose_file" | sed -e 's/^compose\.//' -e 's/\.yml$//'
        fi
    done
}

ensure_password() {
    key="$1"
    current_value="$(get_env "$key")"

    if [ -z "$current_value" ] || [ "$current_value" = "change_me" ]; then
        set_env "$key" "$(openssl rand -hex 32)"
    fi
}

derive_public_key() {
    service="$1"
    private_key="$2"
    private_der="$TEMP_DIR/$service-private.der"

    if ! printf '%s' "$private_key" | openssl base64 -d -A > "$private_der" 2>/dev/null; then
        echo "Invalid base64 private key for $service" >&2
        exit 1
    fi

    if ! openssl pkey -inform DER -in "$private_der" -noout >/dev/null 2>&1; then
        echo "Invalid Ed25519 private key for $service" >&2
        exit 1
    fi

    openssl pkey \
        -inform DER \
        -in "$private_der" \
        -pubout \
        -outform DER 2>/dev/null | openssl base64 -A
}

ensure_signing_key() {
    service="$1"
    variable="$2"
    private_key="$(get_env "$variable")"

    if [ -z "$private_key" ]; then
        private_pem="$TEMP_DIR/$service-private.pem"

        openssl genpkey -algorithm ED25519 -out "$private_pem" >/dev/null 2>&1
        private_key="$(openssl pkey -in "$private_pem" -outform DER | openssl base64 -A)"

        set_env "$variable" "$private_key"
    fi

    derive_public_key "$service" "$private_key"
}

services="$(discover_services)"

if [ -z "$services" ]; then
    echo "No Kafka message-authenticated services were discovered" >&2
    exit 1
fi

ensure_password "KAFKA_BROKER_PASSWORD"
ensure_password "KAFKA_ADMIN_PASSWORD"
remove_env "KAFKA_APP_PASSWORD"

trusted_public_keys=""
old_trusted_public_keys="$(get_env "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS")"

for service in $services; do
    service_variable="$(service_variable_name "$service")"

    ensure_password "KAFKA_${service_variable}_PASSWORD"

    public_key="$(
        ensure_signing_key \
            "$service" \
            "${service_variable}_KAFKA_SIGNING_PRIVATE_KEY"
    )"

    if [ -z "$trusted_public_keys" ]; then
        trusted_public_keys="$service=$public_key"
    else
        trusted_public_keys="$trusted_public_keys;$service=$public_key"
    fi
done

set_env "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS" "$trusted_public_keys"

"$SCRIPT_DIR/verify-kafka-message-auth.sh" "$TARGET_ENV"

if [ "$old_trusted_public_keys" != "$trusted_public_keys" ] && [ -n "$old_trusted_public_keys" ]; then
    echo "Kafka trusted public key set was repaired from the configured private keys."
    echo "Recreate the full stack before processing Kafka messages."
fi

echo "Kafka credentials and Ed25519 message-authentication keys are synchronized"
