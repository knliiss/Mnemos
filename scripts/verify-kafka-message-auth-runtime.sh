#!/usr/bin/env sh
set -eu

TARGET_ENV=".env"
ALLOW_MISSING=0

for argument in "$@"; do
    case "$argument" in
        --allow-missing)
            ALLOW_MISSING=1
            ;;
        *)
            TARGET_ENV="$argument"
            ;;
    esac
done

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

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

get_container_env() {
    container_id="$1"
    key="$2"

    docker inspect \
        --format '{{range .Config.Env}}{{println .}}{{end}}' \
        "$container_id" |
        awk -v key="$key" '
            index($0, key "=") == 1 {
                print substr($0, length(key) + 2)
                exit
            }
        '
}

find_service_container() {
    project_name="$1"
    service="$2"

    docker ps -aq \
        --filter "label=com.docker.compose.project=$project_name" \
        --filter "label=com.docker.compose.service=$service" |
        head -n 1
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

configure_kafka_admin_client() {
    kafka_container_id="$1"
    admin_password="$2"
    tls_store_password="$3"

    printf '%s\n' \
        'security.protocol=SASL_SSL' \
        'sasl.mechanism=PLAIN' \
        "sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username=\"kafka_admin\" password=\"${admin_password}\";" \
        'ssl.truststore.location=/etc/kafka/client-tls/kafka.truststore.p12' \
        "ssl.truststore.password=${tls_store_password}" \
        'ssl.truststore.type=PKCS12' |
        docker exec -i "$kafka_container_id" sh -c \
            'cat > /tmp/mnemos-kafka-acl-verification.properties'
}

verify_kafka_acl_reconciliation() {
    project_name="$1"
    kafka_admin_password="$2"
    kafka_tls_store_password="$3"
    kafka_internal_port="$4"

    kafka_container_id="$(find_service_container "$project_name" "kafka")"
    kafka_init_container_id="$(find_service_container "$project_name" "kafka-init")"

    if [ -z "$kafka_container_id" ] || [ -z "$kafka_init_container_id" ]; then
        if [ "$ALLOW_MISSING" -eq 1 ]; then
            return
        fi

        echo "Kafka containers required for ACL reconciliation verification are missing" >&2
        exit 1
    fi

    kafka_state="$(docker inspect --format '{{.State.Status}}' "$kafka_container_id")"
    kafka_init_state="$(docker inspect --format '{{.State.Status}}' "$kafka_init_container_id")"

    if [ "$kafka_state" != "running" ] || [ "$kafka_init_state" != "exited" ]; then
        if [ "$ALLOW_MISSING" -eq 1 ]; then
            return
        fi

        echo "Kafka ACL reconciliation verification requires running kafka and completed kafka-init containers" >&2
        exit 1
    fi

    configure_kafka_admin_client \
        "$kafka_container_id" \
        "$kafka_admin_password" \
        "$kafka_tls_store_password"

    kafka_acls() {
        docker exec "$kafka_container_id" \
            /opt/kafka/bin/kafka-acls.sh \
            --bootstrap-server "localhost:${kafka_internal_port}" \
            --command-config /tmp/mnemos-kafka-acl-verification.properties \
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
        echo "Kafka bootstrap removed an ACL outside the managed Mnemos service-principal boundary" >&2
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

    echo "Kafka managed ACL reconciliation verified"
}

project_name="$(get_env "COMPOSE_PROJECT_NAME")"
trusted_public_keys="$(get_env "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS")"
kafka_admin_password="$(get_env "KAFKA_ADMIN_PASSWORD")"
kafka_tls_store_password="$(get_env "KAFKA_TLS_STORE_PASSWORD")"
kafka_internal_port="$(get_env "KAFKA_INTERNAL_PORT")"
services="$(discover_services)"

if [ -z "$project_name" ]; then
    echo "COMPOSE_PROJECT_NAME is required" >&2
    exit 1
fi

if [ -z "$trusted_public_keys" ]; then
    echo "MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS is required" >&2
    exit 1
fi

if [ -z "$kafka_admin_password" ] || [ -z "$kafka_tls_store_password" ] || [ -z "$kafka_internal_port" ]; then
    echo "Kafka admin and TLS configuration is required for runtime verification" >&2
    exit 1
fi

verified_count=0

for service in $services; do
    container_id="$(find_service_container "$project_name" "$service")"

    if [ -z "$container_id" ]; then
        if [ "$ALLOW_MISSING" -eq 1 ]; then
            continue
        fi

        echo "Kafka message-authenticated container is missing: $service" >&2
        exit 1
    fi

    state="$(docker inspect --format '{{.State.Status}}' "$container_id")"

    if [ "$state" != "running" ]; then
        if [ "$ALLOW_MISSING" -eq 1 ]; then
            continue
        fi

        echo "Kafka message-authenticated container is not running: $service ($state)" >&2
        exit 1
    fi

    service_variable="$(service_variable_name "$service")"
    expected_private_key="$(get_env "${service_variable}_KAFKA_SIGNING_PRIVATE_KEY")"
    actual_private_key="$(get_container_env "$container_id" "MNEMOS_KAFKA_MESSAGEAUTH_PRIVATEKEY")"
    actual_trusted_public_keys="$(get_container_env "$container_id" "MNEMOS_KAFKA_MESSAGEAUTH_TRUSTEDPUBLICKEYS")"

    if [ "$actual_private_key" != "$expected_private_key" ]; then
        echo "Running $service uses stale Kafka signing-key material; recreate the full stack" >&2
        exit 1
    fi

    if [ "$actual_trusted_public_keys" != "$trusted_public_keys" ]; then
        echo "Running $service uses a stale Kafka trusted-key set; recreate the full stack" >&2
        exit 1
    fi

    verified_count=$((verified_count + 1))
done

echo "Running Kafka message authentication verified for $verified_count services"

verify_kafka_acl_reconciliation \
    "$project_name" \
    "$kafka_admin_password" \
    "$kafka_tls_store_password" \
    "$kafka_internal_port"
