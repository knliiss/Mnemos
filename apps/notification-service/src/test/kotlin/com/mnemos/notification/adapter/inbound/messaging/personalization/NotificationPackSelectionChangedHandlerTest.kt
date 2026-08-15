package com.mnemos.notification.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.notification.application.notification.personalization.usecase.UpdateNotificationPackSelectionService
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateV1
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.Instant
import java.util.UUID

class NotificationPackSelectionChangedHandlerTest {
    private val service = mock<UpdateNotificationPackSelectionService>()
    private val handler = NotificationPackSelectionChangedHandler(service)
    private val userId = UUID.fromString("019c1180-0000-7000-8000-000000000001")
    private val personalizationId = UUID.fromString("019c1180-0000-7000-8000-000000000002")
    private val occurredAt = Instant.parse("2026-08-15T19:00:00Z")

    @Test
    fun `accepts consistent notification pack selection envelope`() {
        assertDoesNotThrow {
            handler.definition.validate(
                envelope(
                    actorUserId = userId,
                    envelopeOccurredAt = occurredAt,
                ),
            )
        }
    }

    @Test
    fun `rejects notification pack selection for different actor`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    actorUserId = UUID.fromString("019c1180-0000-7000-8000-000000000003"),
                    envelopeOccurredAt = occurredAt,
                ),
            )
        }
    }

    @Test
    fun `rejects notification pack selection with different envelope time`() {
        assertThrows(KafkaMessageProtocolException::class.java) {
            handler.definition.validate(
                envelope(
                    actorUserId = userId,
                    envelopeOccurredAt = occurredAt.plusSeconds(1),
                ),
            )
        }
    }

    private fun envelope(
        actorUserId: UUID,
        envelopeOccurredAt: Instant,
    ): EventEnvelope<NotificationPackSelectionChangedV1> =
        EventEnvelope(
            eventId = UUID.fromString("019c1180-0000-7000-8000-000000000004"),
            eventType = PersonalizationEventType.NOTIFICATION_PACK_SELECTION_CHANGED,
            eventVersion = PersonalizationEventContract.VERSION,
            producer = "personalization-service",
            aggregateType = PersonalizationAggregateType.USER_NOTIFICATION_PACK_SELECTION,
            aggregateId = userId,
            occurredAt = envelopeOccurredAt,
            metadata = EventMetadata(actorUserId = actorUserId),
            payload =
                NotificationPackSelectionChangedV1(
                    userId = userId,
                    personalizationId = personalizationId,
                    pack = snapshot(),
                    selectionRevision = 1,
                    occurredAt = occurredAt,
                ),
        )

    private fun snapshot(): NotificationPackSnapshotV1 =
        NotificationPackSnapshotV1(
            mixedAggregateTitle = "Events",
            templates =
                NotificationPackTemplateType.entries.map { type ->
                    NotificationPackTemplateV1(
                        type = type,
                        emoji = null,
                        soloTitle = "${type.name} title",
                        soloBody = "${type.name} body",
                        compact = "${type.name} compact",
                        aggregateTitle = "${type.name} events",
                    )
                },
        )
}
