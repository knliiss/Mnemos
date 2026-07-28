package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.transport.EventDestination
import java.util.UUID

interface OutboxEventWriter {

    fun <T : Any> append(
        destination: EventDestination,
        event: IntegrationEvent<T>,
        metadata: EventMetadata? = null
    ): UUID
}