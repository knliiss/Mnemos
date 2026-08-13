package com.mnemos.messaging.kafka.consumer

import java.util.UUID

fun interface KafkaMessageProcessor {
    fun process(
        consumer: String,
        eventId: UUID,
        handler: () -> Unit,
    ): KafkaMessageProcessingResult
}