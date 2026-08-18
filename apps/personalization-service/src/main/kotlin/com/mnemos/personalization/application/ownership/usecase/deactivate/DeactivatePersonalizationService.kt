package com.mnemos.personalization.application.ownership.usecase.deactivate

import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeactivatePersonalizationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val support: PersonalizationOwnershipSupport,
    private val selectionEventPublisher: NotificationPackSelectionEventPublisher,
    private val profileAppearanceEventPublisher: ProfileAppearanceEventPublisher,
) : DeactivatePersonalizationUseCase {
    @Transactional
    override fun execute(personalizationId: PersonalizationId): OwnedPersonalization {
        val actor = actorProvider.requireActor()
        val personalization = support.requirePersonalization(personalizationId)
        val owner = support.resolveOwner(personalization, actor.userId)
        val ownership =
            ownershipRepository.findForUpdate(owner, personalization.id)
                ?: throw PersonalizationNotOwnedException()
        val wasActive = ownership.active

        ownership.deactivate()

        val saved = support.saveOwnership(ownership)

        when {
            wasActive && owner is PersonalizationOwner.User && personalization.type == PersonalizationType.NOTIFICATION_PACK -> {
                selectionEventPublisher.publishCleared(actor.userId)
            }

            wasActive && owner is PersonalizationOwner.Account -> {
                profileAppearanceEventPublisher.publish(
                    accountId = owner.accountId,
                    actorUserId = actor.userId,
                )
            }
        }

        return saved
    }
}
