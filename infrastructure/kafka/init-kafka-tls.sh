#!/usr/bin/env sh
set -eu

SERVER_TLS_DIR="/var/lib/kafka/server-tls"
CLIENT_TLS_DIR="/var/lib/kafka/client-tls"
KEYSTORE="$SERVER_TLS_DIR/kafka.keystore.p12"
TRUSTSTORE="$CLIENT_TLS_DIR/kafka.truststore.p12"
CERTIFICATE="$SERVER_TLS_DIR/kafka.crt"
STORE_PASSWORD="${KAFKA_TLS_STORE_PASSWORD:-}"
KAFKA_UID=1000
KAFKA_GID=1000

fail() {
    echo "$1" >&2
    exit 1
}

normalize_permissions() {
    chown "$KAFKA_UID:$KAFKA_GID" "$SERVER_TLS_DIR" "$CLIENT_TLS_DIR"
    chown "$KAFKA_UID:$KAFKA_GID" "$KEYSTORE" "$TRUSTSTORE" "$CERTIFICATE"

    chmod 0700 "$SERVER_TLS_DIR"
    chmod 0755 "$CLIENT_TLS_DIR"
    chmod 0600 "$KEYSTORE"
    chmod 0644 "$TRUSTSTORE" "$CERTIFICATE"
}

if [ -z "$STORE_PASSWORD" ]; then
    fail "KAFKA_TLS_STORE_PASSWORD is required"
fi

mkdir -p "$SERVER_TLS_DIR" "$CLIENT_TLS_DIR"

if [ -f "$KEYSTORE" ] && [ -f "$TRUSTSTORE" ] && [ -f "$CERTIFICATE" ]; then
    keytool \
        -list \
        -keystore "$KEYSTORE" \
        -storetype PKCS12 \
        -storepass "$STORE_PASSWORD" >/dev/null 2>&1 ||
        fail "Existing Kafka keystore cannot be opened with KAFKA_TLS_STORE_PASSWORD"

    keytool \
        -list \
        -keystore "$TRUSTSTORE" \
        -storetype PKCS12 \
        -storepass "$STORE_PASSWORD" >/dev/null 2>&1 ||
        fail "Existing Kafka truststore cannot be opened with KAFKA_TLS_STORE_PASSWORD"

    normalize_permissions

    echo "Kafka TLS material is already initialized."
    exit 0
fi

rm -f "$KEYSTORE" "$TRUSTSTORE" "$CERTIFICATE"

keytool \
    -genkeypair \
    -alias kafka \
    -keyalg RSA \
    -keysize 3072 \
    -validity 825 \
    -dname "CN=kafka" \
    -ext "SAN=dns:kafka,dns:localhost,ip:127.0.0.1" \
    -keystore "$KEYSTORE" \
    -storetype PKCS12 \
    -storepass "$STORE_PASSWORD" \
    -keypass "$STORE_PASSWORD" \
    -noprompt

keytool \
    -exportcert \
    -rfc \
    -alias kafka \
    -keystore "$KEYSTORE" \
    -storetype PKCS12 \
    -storepass "$STORE_PASSWORD" \
    -file "$CERTIFICATE"

keytool \
    -importcert \
    -alias kafka \
    -file "$CERTIFICATE" \
    -keystore "$TRUSTSTORE" \
    -storetype PKCS12 \
    -storepass "$STORE_PASSWORD" \
    -noprompt

normalize_permissions

echo "Kafka TLS keystore and client truststore initialized."
