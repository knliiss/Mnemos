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

trusted_key_for() {
    producer="$1"
    trusted_keys="$2"

    awk -v producer="$producer" -v trusted_keys="$trusted_keys" '
        BEGIN {
            count = split(trusted_keys, entries, ";")

            for (index = 1; index <= count; index++) {
                separator = index(entries[index], "=")

                if (separator <= 1) {
                    continue
                }

                name = substr(entries[index], 1, separator - 1)

                if (name == producer) {
                    print substr(entries[index], separator + 1)
                    exit
                }
            }
        }
    '
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

services="$(discover_services)"
trusted_public_keys="$(get_env "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS")"

if [ -z "$services" ]; then
    echo "No Kafka message-authenticated services were discovered" >&2
    exit 1
fi

if [ -z "$trusted_public_keys" ]; then
    echo "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS is required" >&2
    exit 1
fi

service_count=0

for service in $services; do
    service_count=$((service_count + 1))
    service_variable="$(service_variable_name "$service")"
    private_variable="${service_variable}_KAFKA_SIGNING_PRIVATE_KEY"
    private_key="$(get_env "$private_variable")"
    compose_file="$REPOSITORY_ROOT/infrastructure/compose/apps/compose.$service.yml"

    if [ -z "$private_key" ]; then
        echo "$private_variable is required" >&2
        exit 1
    fi

    expected_public_key="$(derive_public_key "$service" "$private_key")"
    trusted_public_key="$(trusted_key_for "$service" "$trusted_public_keys")"

    if [ -z "$trusted_public_key" ]; then
        echo "Trusted public key is missing for $service" >&2
        exit 1
    fi

    if [ "$expected_public_key" != "$trusted_public_key" ]; then
        echo "Trusted public key does not match the configured private key for $service" >&2
        exit 1
    fi

    expected_private_wiring="MNEMOS_KAFKA_MESSAGEAUTH_PRIVATEKEY: \${${private_variable}}"
    expected_trusted_wiring='MNEMOS_KAFKA_MESSAGEAUTH_TRUSTEDPUBLICKEYS: ${MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS}'

    if ! grep -Fq "$expected_private_wiring" "$compose_file"; then
        echo "Kafka private-key wiring is invalid in $compose_file" >&2
        exit 1
    fi

    if ! grep -Fq "$expected_trusted_wiring" "$compose_file"; then
        echo "Kafka trusted-key wiring is invalid in $compose_file" >&2
        exit 1
    fi
done

trusted_entry_count="$(
    printf '%s' "$trusted_public_keys" |
        awk -F';' '{ print NF }'
)"

if [ "$trusted_entry_count" -ne "$service_count" ]; then
    echo "Kafka trusted public key set contains $trusted_entry_count entries, expected $service_count" >&2
    exit 1
fi

printf '%s' "$trusted_public_keys" |
    tr ';' '\n' |
    sed '/^$/d' |
    cut -d= -f1 |
    sort > "$TEMP_DIR/trusted-producers"

printf '%s\n' $services |
    sort > "$TEMP_DIR/services"

if ! cmp -s "$TEMP_DIR/trusted-producers" "$TEMP_DIR/services"; then
    echo "Kafka trusted public key producers do not match the message-authenticated service set" >&2
    exit 1
fi

echo "Kafka message authentication verified for $service_count services"
