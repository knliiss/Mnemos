package com.mnemos.personalization.application.ownership.event

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toAvatarFrameSnapshot
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toNickStyleSnapshot
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toProfileEffectSnapshot
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceContractMapper.toProfileThemeSnapshot
import com.mnemos.personalization.application.ownership.event.port.out.ProfileAppearanceRevisionStore
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class ProfileAppearanceEventPublisher(
    private val publisher: IntegrationEventPublisher,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val revisionStore: ProfileAppearanceRevisionStore,
    private val clock: Clock,
) {
    fun publish(
        accountId: AccountId,
        actorUserId: UserId?,
    ) {
        val owner = PersonalizationOwner.Account(accountId)
        val occurredAt = clock.instant()
        val revision = revisionStore.next(accountId)
        val appearance =
            ProfileAppearanceChangedV1.Appearance(
                nickStyle = activePersonalization(owner, PersonalizationType.NICK_STYLE)?.toNickStyleSnapshot(),
                profileTheme = activePersonalization(owner, PersonalizationType.PROFILE_THEME)?.toProfileThemeSnapshot(),
                avatarFrame = activePersonalization(owner, PersonalizationType.AVATAR_FRAME)?.toAvatarFrameSnapshot(),
                profileEffect = activePersonalization(owner, PersonalizationType.PROFILE_EFFECT)?.toProfileEffectSnapshot(),
            )

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

    private fun activePersonalization(
        owner: PersonalizationOwner.Account,
        type: PersonalizationType,
    ): Personalization? {
        val ownership = ownershipRepository.findActive(owner, type) ?: return null

        return personalizationRepository.findById(ownership.personalizationId)
            ?: error("Active personalization ${ownership.personalizationId.id} was not found")
    }
}
