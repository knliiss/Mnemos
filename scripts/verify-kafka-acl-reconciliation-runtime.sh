#!/usr/bin/env sh
set -eu

TARGET_ENV="${1:-.env}"

if [ ! -f "$TARGET_ENV" ]; then
    echo "Target env file does not exist: $TARGET_ENV" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required" >&2
    exit 1
fi

get_env() {
    key="$1"

    awk -v key="$key" '
        index($0, key "=") == 1 {
            print substr($0, length(key) + 2)
            exit
        }
    ' "$TARGET_ENV"
}

find_service_container() {
    project_name="$1"
    service="$2"

    docker ps -aq \
        --filter "label=com.docker.compose.project=$project_name" \
        --filter "label=com.docker.compose.service=$service" |
        head -n 1
}

require_value() {
    name="$1"
    value="$2"

    if [ -z "$value" ]; then
        echo "$name is required" >&2
        exit 1
    fi
}

project_name="$(get_env "COMPOSE_PROJECT_NAME")"
kafka_admin_password="$(get_env "KAFKA_ADMIN_PASSWORD")"
kafka_tls_store_password="$(get_env "KAFKA_TLS_STORE_PASSWORD")"
kafka_internal_port="$(get_env "KAFKA_INTERNAL_PORT")"

require_value "COMPOSE_PROJECT_NAME" "$project_name"
require_value "KAFKA_ADMIN_PASSWORD" "$kafka_admin_password"
require_value "KAFKA_TLS_STORE_PASSWORD" "$kafka_tls_store_password"
require_value "KAFKA_INTERNAL_PORT" "$kafka_internal_port"

kafka_container_id="$(find_service_container "$project_name" "kafka")"
kafka_init_container_id="$(find_service_container "$project_name" "kafka-init")"

require_value "Kafka container id" "$kafka_container_id"
require_value "Kafka init container id" "$kafka_init_container_id"

kafka_state="$(docker inspect --format '{{.State.Status}}' "$kafka_container_id")"
kafka_init_state="$(docker inspect --format '{{.State.Status}}' "$kafka_init_container_id")"

if [ "$kafka_state" != "running" ]; then
    echo "Kafka must be running for ACL reconciliation verification" >&2
    exit 1
fi

if [ "$kafka_init_state" != "exited" ]; then
    echo "Kafka init must have completed before ACL reconciliation verification" >&2
    exit 1
fi

admin_config="/tmp/mnemos-kafka-acl-reconciliation.properties"

printf '%s\n' \
    'security.protocol=SASL_SSL' \
    'sasl.mechanism=PLAIN' \
    "sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username=\"kafka_admin\" password=\"${kafka_admin_password}\";" \
    'ssl.truststore.location=/etc/kafka/client-tls/kafka.truststore.p12' \
    "ssl.truststore.password=${kafka_tls_store_password}" \
    'ssl.truststore.type=PKCS12' |
    docker exec -i "$kafka_container_id" sh -c "cat > '$admin_config'"

kafka_acls() {
    docker exec "$kafka_container_id" \
        /opt/kafka/bin/kafka-acls.sh \
        --bootstrap-server "localhost:${kafka_internal_port}" \
        --command-config "$admin_config" \
        "$@"
}

stale_topic="mnemos.wallet.commands.v2"
managed_probe_principal="User:account_service"
unmanaged_probe_principal="User:mnemos_acl_probe"

kafka_acls \
    --add \
    --allow-principal "$managed_probe_principal" \
    --operation Read \
    --topic "$stale_topic" >/dev/null

kafka_acls \
    --add \
    --allow-principal "$unmanaged_probe_principal" \
    --operation Read \
    --topic "$stale_topic" >/dev/null

docker start -a "$kafka_init_container_id" >/dev/null

stale_topic_acls="$(kafka_acls --list --topic "$stale_topic")"

if printf '%s\n' "$stale_topic_acls" | grep -Fq "principal=${managed_probe_principal}"; then
    echo "Kafka bootstrap left a stale managed service ACL in place" >&2
    exit 1
fi

if ! printf '%s\n' "$stale_topic_acls" | grep -Fq "principal=${unmanaged_probe_principal}"; then
    echo "Kafka bootstrap removed an ACL outside the managed service-principal boundary" >&2
    exit 1
fi

desired_topic_acls="$(kafka_acls --list --topic "mnemos.account.events.v1")"

if ! printf '%s\n' "$desired_topic_acls" |
    grep -F "principal=${managed_probe_principal}" |
    grep -Fq 'operation=WRITE'; then
    echo "Kafka bootstrap removed a desired managed service ACL" >&2
    exit 1
fi

kafka_acls \
    --remove \
    --force \
    --allow-principal "$unmanaged_probe_principal" \
    --operation Read \
    --topic "$stale_topic" >/dev/null

docker exec "$kafka_container_id" rm -f "$admin_config"

echo "Kafka managed ACL reconciliation verified"
