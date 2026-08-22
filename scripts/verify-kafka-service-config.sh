#!/usr/bin/env sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
REPOSITORY_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)"

fail() {
    echo "$1" >&2
    exit 1
}

has_property() {
    config_file="$1"
    properties_pattern="$2"
    yaml_pattern="$3"

    case "$config_file" in
        *.properties)
            grep -Fqx "$properties_pattern" "$config_file"
            ;;
        *.yml|*.yaml)
            grep -Fq "$yaml_pattern" "$config_file"
            ;;
        *)
            return 1
            ;;
    esac
}

service_config() {
    service_dir="$1"

    if [ -f "$service_dir/src/main/resources/application.properties" ]; then
        printf '%s\n' "$service_dir/src/main/resources/application.properties"
        return
    fi

    if [ -f "$service_dir/src/main/resources/application.yml" ]; then
        printf '%s\n' "$service_dir/src/main/resources/application.yml"
        return
    fi

    if [ -f "$service_dir/src/main/resources/application.yaml" ]; then
        printf '%s\n' "$service_dir/src/main/resources/application.yaml"
        return
    fi

    return 1
}

verified_services=0
listener_services=0
outbox_services=0

for service_dir in "$REPOSITORY_ROOT"/apps/*-service; do
    [ -d "$service_dir" ] || continue

    service="$(basename "$service_dir")"
    kotlin_dir="$service_dir/src/main/kotlin"
    config_file="$(service_config "$service_dir" || true)"
    compose_file="$REPOSITORY_ROOT/infrastructure/compose/apps/compose.$service.yml"
    listener_files=""

    if [ -d "$kotlin_dir" ]; then
        listener_files="$(grep -RIl '@KafkaListener' "$kotlin_dir" 2>/dev/null || true)"
    fi

    has_listener=0
    if [ -n "$listener_files" ]; then
        has_listener=1
        listener_services=$((listener_services + 1))
    fi

    has_outbox=0
    if [ -n "$config_file" ] && grep -Eq '^mnemos\.outbox\.publisher\.enabled=true$' "$config_file"; then
        has_outbox=1
        outbox_services=$((outbox_services + 1))
    fi

    if [ "$has_listener" -eq 0 ] && [ "$has_outbox" -eq 0 ]; then
        continue
    fi

    [ -n "$config_file" ] || fail "$service has Kafka messaging but no application configuration"
    [ -f "$compose_file" ] || fail "$service has Kafka messaging but no compose file"

    grep -Fq 'MNEMOS_KAFKA_MESSAGEAUTH_PRIVATEKEY:' "$compose_file" ||
        fail "$service does not configure its Kafka signing private key"
    grep -Fq 'MNEMOS_KAFKA_MESSAGEAUTH_TRUSTEDPUBLICKEYS: ${MNEMOS_KAFKA_TRUSTED_PUBLIC_KEYS}' "$compose_file" ||
        fail "$service does not configure the shared Kafka trusted public key set"

    has_property \
        "$config_file" \
        'spring.kafka.producer.acks=all' \
        'acks: all' || fail "$service must use Kafka producer acks=all"

    has_property \
        "$config_file" \
        'spring.kafka.producer.properties.enable.idempotence=true' \
        'enable.idempotence: true' || fail "$service must enable Kafka producer idempotence"

    if [ "$has_listener" -eq 1 ]; then
        has_property \
            "$config_file" \
            'spring.kafka.consumer.auto-offset-reset=earliest' \
            'auto-offset-reset: earliest' || fail "$service must consume from earliest when no committed offset exists"

        has_property \
            "$config_file" \
            'spring.kafka.listener.ack-mode=record' \
            'ack-mode: record' || fail "$service must commit Kafka offsets per record"

        for listener_file in $listener_files; do
            grep -Fq 'groupId = "\${spring.application.name}-' "$listener_file" ||
                fail "$listener_file must use the service-prefixed Kafka consumer group convention"
        done

        inbox_migration="$(find "$service_dir/src/main/resources/db/migration" -maxdepth 1 -type f -iname '*inbox*' -print -quit 2>/dev/null || true)"

        if [ -n "$inbox_migration" ]; then
            grep -Eq '^mnemos\.inbox\.enabled=true$' "$config_file" ||
                fail "$service has an inbox migration but mnemos.inbox.enabled is not true"
        fi
    fi

    verified_services=$((verified_services + 1))
done

echo "Kafka service configuration verified: $verified_services services, $listener_services consumers, $outbox_services outbox producers"
