package com.mnemos.personalization.application.ownership.event

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.ownership.event.port.out.ProfileAppearanceRevisionStore
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class ProfileAppearanceEventPublisher(
    private val publisher: IntegrationEventPublisher,
    private val snapshotResolver: ProfileAppearanceSnapshotResolver,
    private val revisionStore: ProfileAppearanceRevisionStore,
    private val clock: Clock,
) {
    fun publish(
        accountId: AccountId,
        actorUserId: UserId?,
    ) {
        val occurredAt = clock.instant()
        val revision = revisionStore.next(accountId)
        val appearance = snapshotResolver.resolve(accountId)

        publisher.publish(
            event =
                IntegrationEvent(
                    type = PersonalizationEventType.PROFILE_APPEARANCE_CHANGED,
                    aggregateId = accountId.id,
                    occurredAt = occurredAt,
                    payload =
                        ProfileAppearanceChangedV1(
                            accountId = accountId.id,
                            appearance = appearance,
                            revision = revision,
                            occurredAt = occurredAt,
                        ),
                ),
            metadata = EventMetadata(actorUserId = actorUserId?.id),
        )
    }
}
