#!/usr/bin/env sh
set -eu

TARGET_ENV="${1:-.env}"

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

broker_password="$(openssl rand -hex 32)"
app_password="$(openssl rand -hex 32)"

set_env "KAFKA_BROKER_PASSWORD" "$broker_password"
set_env "KAFKA_APP_PASSWORD" "$app_password"

services="account-service wallet-service event-service notification-service reward-service bet-service moderation-service gateway-service realtime-service personalization-service"
trusted_public_keys=""

for service in $services; do
    private_pem="$TEMP_DIR/$service-private.pem"

    openssl genpkey -algorithm ED25519 -out "$private_pem" >/dev/null 2>&1

    private_key="$(openssl pkey -in "$private_pem" -outform DER | openssl base64 -A)"
    public_key="$(openssl pkey -in "$private_pem" -pubout -outform DER | openssl base64 -A)"
    variable_name="$(printf '%s' "$service" | tr '[:lower:]-' '[:upper:]_')_KAFKA_SIGNING_PRIVATE_KEY"

    set_env "$variable_name" "$private_key"

    if [ -z "$trusted_public_keys" ]; then
        trusted_public_keys="$service=$public_key"
    else
        trusted_public_keys="$trusted_public_keys;$service=$public_key"
    fi
done

set_env "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS" "$trusted_public_keys"

secrets_dir=".secrets/kafka"
mkdir -p "$secrets_dir"
chmod 700 ".secrets" "$secrets_dir"

cat > "$secrets_dir/kafka_server_jaas.conf" <<EOF
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="broker"
    password="$broker_password"
    user_broker="$broker_password"
    user_mnemos_apps="$app_password";
};
EOF

chmod 600 "$secrets_dir/kafka_server_jaas.conf"

echo "Kafka broker credentials and Ed25519 message signing keys updated"
