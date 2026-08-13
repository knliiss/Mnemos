package com.mnemos.messaging.kafka.consumer

import java.util.UUID

class DirectKafkaMessageProcessor : KafkaMessageProcessor {
    override fun process(
        consumer: String,
        eventId: UUID,
        handler: () -> Unit,
    ): KafkaMessageProcessingResult {
        handler()

        return KafkaMessageProcessingResult.PROCESSED
    }
}