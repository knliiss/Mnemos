#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"
KAFKA_COMPOSE="$REPOSITORY_ROOT/infrastructure/compose/compose.kafka.yml"
KAFKA_BOOTSTRAP="$REPOSITORY_ROOT/infrastructure/kafka/KafkaBootstrap.java"

fail() {
    echo "$1" >&2
    exit 1
}

require_text() {
    file="$1"
    text="$2"
    message="$3"

    grep -Fq "$text" "$file" || fail "$message"
}

require_text \
    "$KAFKA_COMPOSE" \
    'INTERNAL:SASL_SSL,EXTERNAL:SASL_PLAINTEXT,CONTROLLER:SASL_SSL' \
    "Kafka internal and controller listeners must use SASL_SSL"

require_text \
    "$KAFKA_COMPOSE" \
    'KAFKA_SASL_MECHANISM_CONTROLLER_PROTOCOL=PLAIN' \
    "Kafka controller SASL mechanism is missing"

require_text \
    "$KAFKA_COMPOSE" \
    'KAFKA_LISTENER_NAME_CONTROLLER_PLAIN_SASL_JAAS_CONFIG=' \
    "Kafka controller JAAS authentication is missing"

if grep -Fq 'User:ANONYMOUS' "$KAFKA_COMPOSE"; then
    fail "Kafka must not grant superuser privileges to ANONYMOUS"
fi

require_text \
    "$KAFKA_COMPOSE" \
    'kafka-tls-init:' \
    "Kafka TLS bootstrap service is missing"

require_text \
    "$KAFKA_COMPOSE" \
    'kafka-server-tls:/etc/kafka/server-tls:ro' \
    "Kafka server keystore is not mounted read-only"

require_text \
    "$KAFKA_COMPOSE" \
    'kafka-client-tls:/etc/kafka/client-tls:ro' \
    "Kafka client truststore is not mounted read-only"

require_text \
    "$KAFKA_BOOTSTRAP" \
    'properties.put("security.protocol", "SASL_SSL");' \
    "Kafka bootstrap client must use SASL_SSL"

require_text \
    "$KAFKA_BOOTSTRAP" \
    'properties.put("ssl.truststore.location", tlsTruststoreLocation);' \
    "Kafka bootstrap client truststore is missing"

verified_clients=0

for compose_file in "$REPOSITORY_ROOT"/infrastructure/compose/apps/compose.*-service.yml; do
    [ -f "$compose_file" ] || continue

    if ! grep -Fq 'SPRING_KAFKA_BOOTSTRAP_SERVERS:' "$compose_file"; then
        continue
    fi

    require_text \
        "$compose_file" \
        'SPRING_KAFKA_PROPERTIES_SECURITY_PROTOCOL: SASL_SSL' \
        "$(basename "$compose_file") must use SASL_SSL"

    require_text \
        "$compose_file" \
        'SPRING_KAFKA_PROPERTIES_SSL_TRUSTSTORE_LOCATION: /etc/mnemos/kafka/kafka.truststore.p12' \
        "$(basename "$compose_file") is missing the Kafka truststore location"

    require_text \
        "$compose_file" \
        'SPRING_KAFKA_PROPERTIES_SSL_TRUSTSTORE_PASSWORD: ${KAFKA_TLS_STORE_PASSWORD}' \
        "$(basename "$compose_file") is missing the Kafka truststore password"

    require_text \
        "$compose_file" \
        'SPRING_KAFKA_PROPERTIES_SSL_TRUSTSTORE_TYPE: PKCS12' \
        "$(basename "$compose_file") is missing the Kafka truststore type"

    require_text \
        "$compose_file" \
        'kafka-client-tls:/etc/mnemos/kafka:ro' \
        "$(basename "$compose_file") does not mount the Kafka truststore read-only"

    verified_clients=$((verified_clients + 1))
done

if [ "$verified_clients" -eq 0 ]; then
    fail "No Docker Kafka clients were verified"
fi

echo "Kafka transport security verified: $verified_clients application clients"
