package com.mnemos.messaging.kafka.consumer

import com.mnemos.messaging.core.event.EventEnvelope

class KafkaMessageDefinition<T : Any>(
    val topic: String,
    val type: String,
    val version: Int,
    val aggregateType: String,
    val payloadType: Class<T>,
    val validate: (EventEnvelope<T>) -> Unit = {},
) {
    init {
        require(TOPIC_PATTERN.matches(topic)) {
            "Invalid Kafka topic: $topic"
        }

        require(TYPE_PATTERN.matches(type)) {
            "Invalid Kafka message type: $type"
        }

        require(version > 0) {
            "Kafka message version must be positive"
        }

        require(AGGREGATE_TYPE_PATTERN.matches(aggregateType)) {
            "Invalid aggregate type: $aggregateType"
        }
    }

    private companion object {
        val TOPIC_PATTERN = Regex("^[a-zA-Z0-9._-]+$")
        val TYPE_PATTERN = Regex("^[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9-]*)+$")
        val AGGREGATE_TYPE_PATTERN = Regex("^[A-Z][A-Z0-9_]*$")
    }
}

inline fun <reified T : Any> kafkaMessageDefinition(
    topic: String,
    type: String,
    version: Int,
    aggregateType: String,
    noinline validate: (EventEnvelope<T>) -> Unit = {},
): KafkaMessageDefinition<T> = KafkaMessageDefinition(
    topic = topic,
    type = type,
    version = version,
    aggregateType = aggregateType,
    payloadType = T::class.java,
    validate = validate,
)