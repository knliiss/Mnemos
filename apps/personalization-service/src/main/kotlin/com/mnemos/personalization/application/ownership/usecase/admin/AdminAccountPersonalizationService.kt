package com.mnemos.personalization.application.ownership.usecase.admin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.personalization.application.ownership.event.ProfileAppearanceEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.support.PersonalizationOwnershipSupport
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class AdminAccountPersonalizationService(
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val ownershipSupport: PersonalizationOwnershipSupport,
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val profileAppearanceEventPublisher: ProfileAppearanceEventPublisher,
    private val clock: Clock,
) {
    @Transactional(readOnly = true)
    fun list(accountId: AccountId): List<OwnedPersonalization> =
        ownershipRepository.findAll(PersonalizationOwner.Account(accountId))

    @Transactional
    fun grant(
        accountId: AccountId,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization {
        val personalization = ownershipSupport.requirePersonalizationForUpdate(personalizationId)
        requireAccountPersonalization(personalization.type.target)

        val owner = PersonalizationOwner.Account(accountId)
        val existing = ownershipRepository.findForUpdate(owner, personalizationId)

        if (existing != null) {
            return existing
        }

        return ownershipSupport.saveOwnership(
            OwnedPersonalization(
                owner = owner,
                personalizationId = personalization.id,
                type = personalization.type,
                acquiredAt = clock.instant(),
                active = false,
            ),
        )
    }

    @Transactional
    fun activate(
        accountId: AccountId,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization {
        val owner = PersonalizationOwner.Account(accountId)
        val ownership =
            ownershipRepository.findForUpdate(owner, personalizationId)
                ?: throw PersonalizationNotOwnedException()

        ownershipRepository.deactivateActive(
            owner = owner,
            type = ownership.type,
        )
        ownership.activate()

        val saved = ownershipSupport.saveOwnership(ownership)
        publishAppearanceChanged(accountId)

        return saved
    }

    @Transactional
    fun deactivate(
        accountId: AccountId,
        personalizationId: PersonalizationId,
    ): OwnedPersonalization {
        val owner = PersonalizationOwner.Account(accountId)
        val ownership =
            ownershipRepository.findForUpdate(owner, personalizationId)
                ?: throw PersonalizationNotOwnedException()

        if (!ownership.active) {
            return ownership
        }

        ownership.deactivate()

        val saved = ownershipSupport.saveOwnership(ownership)
        publishAppearanceChanged(accountId)

        return saved
    }

    @Transactional
    fun revoke(
        accountId: AccountId,
        personalizationId: PersonalizationId,
    ) {
        val owner = PersonalizationOwner.Account(accountId)
        val ownership =
            ownershipRepository.findForUpdate(owner, personalizationId)
                ?: throw PersonalizationNotOwnedException()
        val appearanceChanged = ownership.active

        ownershipRepository.delete(
            owner = owner,
            personalizationId = personalizationId,
        )

        if (appearanceChanged) {
            publishAppearanceChanged(accountId)
        }
    }

    private fun requireAccountPersonalization(target: PersonalizationTarget) {
        if (target != PersonalizationTarget.ACCOUNT) {
            throw PersonalizationAccessDeniedException()
        }
    }

    private fun publishAppearanceChanged(accountId: AccountId) {
        val actor = actorProvider.requireActor()

        profileAppearanceEventPublisher.publish(
            accountId = accountId,
            actorUserId = actor.userId,
        )
    }
}
