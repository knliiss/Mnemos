package com.mnemos.account.support

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.outbox.writer.OutboxEventWriter
import java.util.UUID

class RecordingOutboxEventWriter : OutboxEventWriter {

    val events = mutableListOf<RecordedEvent>()

    override fun <T : Any> append(
        destination: EventDestination,
        event: IntegrationEvent<T>,
        metadata: EventMetadata?
    ): UUID {
        val eventId = UUID.randomUUID()

        events += RecordedEvent(
            eventId = eventId,
            destination = destination,
            event = event,
            metadata = metadata
        )

        return eventId
    }

    data class RecordedEvent(
        val eventId: UUID,
        val destination: EventDestination,
        val event: IntegrationEvent<*>,
        val metadata: EventMetadata?
    )
}