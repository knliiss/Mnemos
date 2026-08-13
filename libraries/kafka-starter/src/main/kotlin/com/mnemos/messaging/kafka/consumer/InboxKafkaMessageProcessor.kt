package com.mnemos.messaging.kafka.consumer

import com.mnemos.messaging.inbox.InboxEventProcessor
import com.mnemos.messaging.inbox.InboxProcessingResult
import java.util.UUID

class InboxKafkaMessageProcessor(
    private val inboxEventProcessor: InboxEventProcessor,
) : KafkaMessageProcessor {
    override fun process(
        consumer: String,
        eventId: UUID,
        handler: () -> Unit,
    ): KafkaMessageProcessingResult =
        when (
            inboxEventProcessor.process(
                consumer = consumer,
                eventId = eventId,
                handler = handler,
            )
        ) {
            InboxProcessingResult.PROCESSED -> KafkaMessageProcessingResult.PROCESSED
            InboxProcessingResult.DUPLICATE -> KafkaMessageProcessingResult.DUPLICATE
        }
}