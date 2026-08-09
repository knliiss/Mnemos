package com.mnemos.messaging.kafka.consumer

object KafkaDeadLetterTopic {

    const val SUFFIX =
        ".dlt"

    fun name(
        sourceTopic: String,
    ): String {
        require(
            sourceTopic.isNotBlank(),
        ) {
            "Source Kafka topic cannot be blank"
        }

        return sourceTopic +
                SUFFIX
    }
}