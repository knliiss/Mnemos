package com.mnemos.messaging.core.event

import java.time.Instant
import java.util.UUID

data class EventEnvelope<T : Any>(
    val eventId: UUID,
    val eventType: String,
    val eventVersion: Int,
    val producer: String,
    val aggregateType: String,
    val aggregateId: UUID,
    val occurredAt: Instant,
    val metadata: EventMetadata,
    val payload: T
) {
    init {
        require(eventType.isNotBlank()) {
            "Event type cannot be blank"
        }

        require(eventVersion > 0) {
            "Event version must be positive"
        }

        require(PRODUCER_PATTERN.matches(producer)) {
            "Producer must use lowercase kebab case, for example account-service"
        }

        require(aggregateType.isNotBlank()) {
            "Aggregate type cannot be blank"
        }
    }

    companion object {
        fun <T : Any> from(
            eventId: UUID,
            producer: String,
            metadata: EventMetadata,
            event: IntegrationEvent<T>
        ): EventEnvelope<T> = EventEnvelope(
            eventId = eventId,
            eventType = event.eventType,
            eventVersion = event.eventVersion,
            producer = producer,
            aggregateType = event.aggregateType,
            aggregateId = event.aggregateId,
            occurredAt = event.occurredAt,
            metadata = metadata,
            payload = event.payload
        )

        private val PRODUCER_PATTERN =
            Regex("^[a-z][a-z0-9]*(?:-[a-z0-9]+)*$")
    }
}