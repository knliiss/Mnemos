package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import java.util.UUID

interface OutboxEventWriter {

    fun <T : Any> append(
        route: IntegrationEventRoute,
        event: IntegrationEvent<T>,
        metadata: EventMetadata,
    ): UUID
}