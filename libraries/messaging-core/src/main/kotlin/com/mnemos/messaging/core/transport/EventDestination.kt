package com.mnemos.messaging.core.transport

data class EventDestination(
    val topic: String,
    val key: String
) {
    init {
        require(TOPIC_PATTERN.matches(topic)) {
            "Kafka topic contains unsupported characters: $topic"
        }

        require(key.isNotBlank()) {
            "Event key cannot be blank"
        }
    }

    private companion object {
        val TOPIC_PATTERN =
            Regex("^[a-zA-Z0-9._-]+$")
    }
}