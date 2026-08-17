package com.mnemos.personalization.application.ownership.usecase.activate

import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.exception.PersonalizationOwnershipConflictException
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ActivatePersonalizationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val support: PersonalizationOwnershipSupport,
    private val selectionEventPublisher: NotificationPackSelectionEventPublisher,
    private val profileAppearanceEventPublisher: ProfileAppearanceEventPublisher,
    private val clock: Clock,
) : ActivatePersonalizationUseCase {
    @Transactional
    override fun execute(personalizationId: PersonalizationId): OwnedPersonalization {
        val actor = actorProvider.requireActor()
        val personalization = support.requirePersonalizationForUpdate(personalizationId)
        val owner = support.resolveOwner(personalization, actor.userId)
        var ownership = ownershipRepository.findForUpdate(owner, personalization.id)

        if (ownership == null) {
            if (!support.canCreatorClaim(personalization, actor.userId)) {
                throw PersonalizationNotOwnedException()
            }

            if (support.hasPendingPurchase(owner, personalization.id)) {
                throw PersonalizationOwnershipConflictException()
            }

            ownership =
                support.saveOwnership(
                    OwnedPersonalization(
                        owner = owner,
                        personalizationId = personalization.id,
                        type = personalization.type,
                        acquiredAt = clock.instant(),
                        active = false,
                    ),
                )
        }

        ownershipRepository.deactivateActive(
            owner = owner,
            type = personalization.type,
        )
        ownership.activate()

        val saved = support.saveOwnership(ownership)

        when {
            owner is PersonalizationOwner.User && personalization.type == PersonalizationType.NOTIFICATION_PACK -> {
                selectionEventPublisher.publishSelected(
                    userId = actor.userId,
                    personalization = personalization,
                )
            }

            owner is PersonalizationOwner.Account -> {
                profileAppearanceEventPublisher.publish(
                    accountId = owner.accountId,
                    actorUserId = actor.userId,
                )
            }
        }

        return saved
    }
}
