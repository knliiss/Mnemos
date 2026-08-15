#!/bin/sh
set -eu

BOOTSTRAP_SERVER="${KAFKA_BOOTSTRAP_SERVER:-kafka:29092}"
ADMIN_CONFIG=/tmp/kafka-admin.properties

cat > "$ADMIN_CONFIG" <<EOF
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka_admin" password="${KAFKA_ADMIN_PASSWORD}";
EOF

kafka_topics() {
    /opt/kafka/bin/kafka-topics.sh \
        --bootstrap-server "$BOOTSTRAP_SERVER" \
        --command-config "$ADMIN_CONFIG" \
        "$@"
}

kafka_acls() {
    /opt/kafka/bin/kafka-acls.sh \
        --bootstrap-server "$BOOTSTRAP_SERVER" \
        --command-config "$ADMIN_CONFIG" \
        "$@"
}

create_topic() {
    topic="$1"
    partitions="${2:-3}"
    shift 2 || true

    kafka_topics \
        --create \
        --if-not-exists \
        --topic "$topic" \
        --partitions "$partitions" \
        --replication-factor 1 \
        "$@"
}

allow_topic_read() {
    principal="$1"
    topic="$2"

    kafka_acls \
        --add \
        --allow-principal "User:$principal" \
        --operation Read \
        --operation Describe \
        --topic "$topic"
}

allow_topic_write() {
    principal="$1"
    topic="$2"

    kafka_acls \
        --add \
        --allow-principal "User:$principal" \
        --operation Write \
        --operation Describe \
        --topic "$topic"
}

allow_group_prefix() {
    principal="$1"
    prefix="$2"

    kafka_acls \
        --add \
        --allow-principal "User:$principal" \
        --operation Read \
        --group "$prefix" \
        --resource-pattern-type prefixed
}

allow_consumer() {
    principal="$1"
    service_name="$2"
    topic="$3"

    allow_topic_read "$principal" "$topic"
    allow_topic_write "$principal" "$topic.dlt"
    allow_group_prefix "$principal" "$service_name-"
}

create_topic mnemos.account.events.v1 3
create_topic mnemos.account.events.v1.dlt 3
create_topic mnemos.event.events.v1 3
create_topic mnemos.event.events.v1.dlt 3
create_topic mnemos.wallet.commands.v2 3
create_topic mnemos.wallet.commands.v2.dlt 3
create_topic mnemos.wallet.events.v2 3
create_topic mnemos.wallet.events.v2.dlt 3
create_topic mnemos.bet.events.v1 3
create_topic mnemos.bet.events.v1.dlt 3
create_topic mnemos.moderation.events.v1 3
create_topic mnemos.moderation.events.v1.dlt 3
create_topic mnemos.personalization.events.v1 3
create_topic mnemos.personalization.events.v1.dlt 3
create_topic \
    mnemos.collector.ingress.v2 \
    1 \
    --config cleanup.policy=delete \
    --config retention.ms=86400000 \
    --config retention.bytes=268435456 \
    --config segment.ms=300000 \
    --config segment.bytes=67108864 \
    --config compression.type=lz4
create_topic \
    mnemos.collector.ingress.v2.dlt \
    1 \
    --config cleanup.policy=delete \
    --config retention.ms=604800000 \
    --config retention.bytes=67108864

allow_topic_write account_service mnemos.account.events.v1

allow_consumer wallet_service wallet-service mnemos.wallet.commands.v2
allow_topic_write wallet_service mnemos.wallet.events.v2

allow_consumer event_service event-service mnemos.collector.ingress.v2
allow_topic_write event_service mnemos.event.events.v1

allow_consumer reward_service reward-service mnemos.event.events.v1
allow_topic_write reward_service mnemos.wallet.commands.v2

allow_consumer bet_service bet-service mnemos.event.events.v1
allow_consumer bet_service bet-service mnemos.wallet.events.v2
allow_topic_write bet_service mnemos.bet.events.v1
allow_topic_write bet_service mnemos.wallet.commands.v2

allow_topic_write moderation_service mnemos.moderation.events.v1

allow_consumer gateway_service gateway-service mnemos.moderation.events.v1

allow_consumer notification_service notification-service mnemos.account.events.v1
allow_consumer notification_service notification-service mnemos.event.events.v1
allow_consumer notification_service notification-service mnemos.moderation.events.v1
allow_consumer notification_service notification-service mnemos.personalization.events.v1

allow_topic_write realtime_service mnemos.collector.ingress.v2
allow_consumer realtime_service realtime-service mnemos.wallet.events.v2
allow_consumer realtime_service realtime-service mnemos.bet.events.v1
allow_consumer realtime_service realtime-service mnemos.personalization.events.v1

allow_consumer personalization_service personalization-service mnemos.wallet.events.v2
allow_consumer personalization_service personalization-service mnemos.personalization.events.v1
allow_topic_write personalization_service mnemos.wallet.commands.v2
allow_topic_write personalization_service mnemos.personalization.events.v1

printf '%s\n' 'Kafka topics and ACLs are configured.'
