package com.mnemos.personalization.application.personalization.support

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Component

@Component
class PersonalizationCreatorSupport(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val selectionEventPublisher: NotificationPackSelectionEventPublisher,
) {
    fun requireActor(): PersonalizationActor = actorProvider.requireActor()

    fun requireMarketplaceActor(): PersonalizationActor {
        val actor = actorProvider.requireActor()

        if (!actor.canCreateMarketplacePersonalization()) {
            throw PersonalizationAccessDeniedException()
        }

        return actor
    }

    fun requireOwnedForUpdate(
        personalizationId: PersonalizationId,
        userId: UserId,
    ): Personalization {
        val personalization =
            repository.findByIdForUpdate(personalizationId)
                ?: throw PersonalizationNotFoundException()

        if (personalization.creatorUserId != userId) {
            throw PersonalizationAccessDeniedException()
        }

        return personalization
    }

    fun requireCustomPackOwnershipBeforeSubmission(
        personalization: Personalization,
        userId: UserId,
    ) {
        if (personalization.origin != PersonalizationOrigin.CUSTOM) {
            return
        }

        val owned =
            ownershipRepository.exists(
                owner = PersonalizationOwner.User(userId),
                personalizationId = personalization.id,
            )

        if (!owned) {
            throw PersonalizationNotOwnedException()
        }
    }

    fun republishActiveNotificationPack(
        personalization: Personalization,
        userId: UserId,
    ) {
        val activeOwnership =
            ownershipRepository.findActiveForUpdate(
                owner = PersonalizationOwner.User(userId),
                type = PersonalizationType.NOTIFICATION_PACK,
            ) ?: return

        if (activeOwnership.personalizationId != personalization.id) {
            return
        }

        selectionEventPublisher.publishSelected(
            userId = userId,
            personalization = personalization,
        )
    }
}
