package com.mnemos.personalization.application.ownership

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.exception.ActiveAccountRequiredException
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class PersonalizationOwnershipService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val accountAccess: PersonalizationAccountAccess,
    private val personalizationRepository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val clock: Clock,
) {
    @Transactional
    fun activate(personalizationId: PersonalizationId): OwnedPersonalization {
        val actor = actorProvider.requireActor()
        val personalization = requirePersonalization(personalizationId)
        val owner = resolveOwner(personalization, actor.userId)

        var ownership = ownershipRepository.find(owner, personalization.id)

        if (ownership == null) {
            if (!canCreatorClaim(personalization, actor.userId)) {
                throw PersonalizationNotOwnedException()
            }

            ownership =
                ownershipRepository.save(
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

        return ownershipRepository.save(ownership)
    }

    @Transactional
    fun deactivate(personalizationId: PersonalizationId): OwnedPersonalization {
        val actor = actorProvider.requireActor()
        val personalization = requirePersonalization(personalizationId)
        val owner = resolveOwner(personalization, actor.userId)
        val ownership =
            ownershipRepository.find(owner, personalization.id)
                ?: throw PersonalizationNotOwnedException()

        ownership.deactivate()

        return ownershipRepository.save(ownership)
    }

    fun listMine(): List<OwnedPersonalization> {
        val actor = actorProvider.requireActor()
        val result =
            ownershipRepository
                .findAll(PersonalizationOwner.User(actor.userId))
                .toMutableList()

        try {
            val accountId = accountAccess.requireActiveAccount(actor.userId)
            result += ownershipRepository.findAll(PersonalizationOwner.Account(accountId))
        } catch (_: ActiveAccountRequiredException) {
            // A user can own user-scoped personalization without having an active account.
        }

        return result
    }

    private fun resolveOwner(
        personalization: Personalization,
        userId: UserId,
    ): PersonalizationOwner =
        when (personalization.type.target) {
            PersonalizationTarget.USER -> PersonalizationOwner.User(userId)
            PersonalizationTarget.ACCOUNT -> {
                val accountId = accountAccess.requireActiveAccount(userId)
                val granted =
                    accountAccess.hasPermission(
                        accountId = accountId,
                        userId = userId,
                        permission = RolePermission.APPLY_PERSONALIZATIONS,
                    )

                if (!granted) {
                    throw PersonalizationAccessDeniedException()
                }

                PersonalizationOwner.Account(accountId)
            }
        }

    private fun requirePersonalization(personalizationId: PersonalizationId): Personalization =
        personalizationRepository.findById(personalizationId)
            ?: throw PersonalizationNotFoundException()

    private fun canCreatorClaim(
        personalization: Personalization,
        userId: UserId,
    ): Boolean =
        personalization.creatorUserId == userId &&
            personalization.visibility == PersonalizationVisibility.PUBLIC &&
            (
                personalization.status == PersonalizationStatus.PUBLISHED ||
                    personalization.status == PersonalizationStatus.UNLISTED
            )
}
