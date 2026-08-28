#!/usr/bin/env sh

set -eu

if [ "$(id -u)" -ne 0 ]; then
    echo "This script must run as root" >&2
    exit 1
fi

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-mnemos}"
VOLUME_NAME="${MNEMOS_KAFKA_CLIENT_TLS_VOLUME:-${COMPOSE_PROJECT_NAME}-kafka-client-tls}"
CONFIG_PREFIX="${MNEMOS_KAFKA_TRUSTSTORE_CONFIG_PREFIX:-mnemos-kafka-truststore}"

VOLUME_MOUNTPOINT="$(
    docker volume inspect \
        --format '{{ .Mountpoint }}' \
        "$VOLUME_NAME"
)"
TRUSTSTORE_PATH="$VOLUME_MOUNTPOINT/kafka.truststore.p12"

if [ ! -s "$TRUSTSTORE_PATH" ]; then
    echo "Kafka client truststore is missing or empty: $TRUSTSTORE_PATH" >&2
    exit 1
fi

TRUSTSTORE_SHA256="$(sha256sum "$TRUSTSTORE_PATH" | awk '{print $1}')"
CONFIG_NAME="${CONFIG_PREFIX}-${TRUSTSTORE_SHA256}"

if ! docker config inspect "$CONFIG_NAME" >/dev/null 2>&1; then
    echo "Creating immutable Swarm Kafka truststore config: $CONFIG_NAME" >&2
    docker config create "$CONFIG_NAME" "$TRUSTSTORE_PATH" >/dev/null
fi

printf '%s\n' "$CONFIG_NAME"
