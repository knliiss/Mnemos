package com.mnemos.messaging.outbox.publisher

import com.mnemos.messaging.core.event.EventHeaders
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.core.transport.EventMessage
import java.time.Instant
import java.util.UUID

data class OutboxEventRecord(
    val id: UUID,
    val topic: String,
    val eventKey: String,

    val eventType: String,
    val eventVersion: Int,
    val producer: String,

    val aggregateType: String,
    val aggregateId: UUID,

    val correlationId: String?,
    val causationId: UUID?,
    val actorUserId: UUID?,

    val envelope: String,
    val occurredAt: Instant,

    val attempts: Int
) {
    fun toEventMessage(): EventMessage =
        EventMessage(
            destination = EventDestination(
                topic = topic,
                key = eventKey
            ),
            payload = envelope,
            headers = buildMap {
                put(EventHeaders.EVENT_ID, id.toString())
                put(EventHeaders.EVENT_TYPE, eventType)
                put(EventHeaders.EVENT_VERSION, eventVersion.toString())

                put(EventHeaders.PRODUCER, producer)

                put(EventHeaders.AGGREGATE_TYPE, aggregateType)
                put(EventHeaders.AGGREGATE_ID, aggregateId.toString())

                put(EventHeaders.OCCURRED_AT, occurredAt.toString())

                correlationId?.let {
                    put(EventHeaders.CORRELATION_ID, it)
                }

                causationId?.let {
                    put(EventHeaders.CAUSATION_ID, it.toString())
                }

                actorUserId?.let {
                    put(EventHeaders.ACTOR_USER_ID, it.toString())
                }
            }
        )
}