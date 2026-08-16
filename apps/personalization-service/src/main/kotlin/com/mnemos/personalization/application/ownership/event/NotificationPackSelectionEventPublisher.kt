package com.mnemos.personalization.application.ownership.event

import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.ownership.event.NotificationPackContractMapper.toContract
import com.mnemos.personalization.application.ownership.event.port.out.NotificationPackSelectionRevisionStore
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.NotificationPackSelectionChangedV1
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class NotificationPackSelectionEventPublisher(
    private val publisher: IntegrationEventPublisher,
    private val revisionStore: NotificationPackSelectionRevisionStore,
    private val clock: Clock,
) {
    fun publishSelected(
        userId: UserId,
        personalization: Personalization,
    ) {
        val content =
            personalization.content as? PersonalizationContent.NotificationPack
                ?: error("Notification pack personalization must contain notification pack content")
        val selectionRevision = revisionStore.next(userId)
        val occurredAt = clock.instant()

        publisher.publish(
            event =
                IntegrationEvent(
                    type = PersonalizationEventType.NOTIFICATION_PACK_SELECTION_CHANGED,
                    aggregateId = userId.id,
                    occurredAt = occurredAt,
                    payload =
                        NotificationPackSelectionChangedV1(
                            userId = userId.id,
                            personalizationId = personalization.id.id,
                            pack = content.toContract(),
                            selectionRevision = selectionRevision,
                            occurredAt = occurredAt,
                        ),
                ),
            metadata = EventMetadata(actorUserId = userId.id),
        )
    }

    fun publishCleared(userId: UserId) {
        val selectionRevision = revisionStore.next(userId)
        val occurredAt = clock.instant()

        publisher.publish(
            event =
                IntegrationEvent(
                    type = PersonalizationEventType.NOTIFICATION_PACK_SELECTION_CHANGED,
                    aggregateId = userId.id,
                    occurredAt = occurredAt,
                    payload =
                        NotificationPackSelectionChangedV1(
                            userId = userId.id,
                            personalizationId = null,
                            pack = null,
                            selectionRevision = selectionRevision,
                            occurredAt = occurredAt,
                        ),
                ),
            metadata = EventMetadata(actorUserId = userId.id),
        )
    }
}
