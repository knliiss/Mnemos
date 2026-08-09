package com.mnemos.messaging.core.publisher

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import java.util.UUID

interface IntegrationEventPublisher {

    fun <T : Any> publish(
        event: IntegrationEvent<T>,
        metadata: EventMetadata = EventMetadata(),
    ): UUID
}