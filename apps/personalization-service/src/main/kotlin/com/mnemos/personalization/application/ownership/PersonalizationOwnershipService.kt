package com.mnemos.personalization.application.ownership

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.exception.ActiveAccountRequiredException
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.ownership.event.NotificationPackSelectionEventPublisher
import com.mnemos.personalization.application.ownership.exception.PersonalizationNotOwnedException
import com.mnemos.personalization.application.ownership.exception.PersonalizationOwnershipConflictException
import com.mnemos.personalization.application.ownership.port.out.OwnedPersonalizationReader
import com.mnemos.personalization.application.ownership.query.ListOwnedPersonalizationsQuery
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
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class PersonalizationOwnershipService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val accountAccess: PersonalizationAccountAccess,
    private val personalizationRepository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val ownershipReader: OwnedPersonalizationReader,
    private val purchaseOperationRepository: PurchaseOperationRepository,
    private val notificationPackSelectionEventPublisher: NotificationPackSelectionEventPublisher,
    private val clock: Clock,
) {
    @Transactional
    fun activate(personalizationId: PersonalizationId): OwnedPersonalization {
        val actor = actorProvider.requireActor()
        val personalization = requirePersonalizationForUpdate(personalizationId)
        val owner = resolveOwner(personalization, actor.userId)

        var ownership = ownershipRepository.findForUpdate(owner, personalization.id)

        if (ownership == null) {
            if (!canCreatorClaim(personalization, actor.userId)) {
                throw PersonalizationNotOwnedException()
            }

            if (purchaseOperationRepository.existsPending(owner, personalization.id)) {
                throw PersonalizationOwnershipConflictException()
            }

            ownership =
                saveOwnership(
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
        val saved = saveOwnership(ownership)

        if (owner is PersonalizationOwner.User && personalization.type == PersonalizationType.NOTIFICATION_PACK) {
            notificationPackSelectionEventPublisher.publishSelected(
                userId = actor.userId,
                personalization = personalization,
            )
        }

        return saved
    }

    @Transactional
    fun deactivate(personalizationId: PersonalizationId): OwnedPersonalization {
        val actor = actorProvider.requireActor()
        val personalization = requirePersonalization(personalizationId)
        val owner = resolveOwner(personalization, actor.userId)
        val ownership =
            ownershipRepository.findForUpdate(owner, personalization.id)
                ?: throw PersonalizationNotOwnedException()
        val wasActive = ownership.active

        ownership.deactivate()
        val saved = saveOwnership(ownership)

        if (
            wasActive &&
            owner is PersonalizationOwner.User &&
            personalization.type == PersonalizationType.NOTIFICATION_PACK
        ) {
            notificationPackSelectionEventPublisher.publishCleared(actor.userId)
        }

        return saved
    }

    fun listMine(
        query: ListOwnedPersonalizationsQuery,
    ): PageResult<OwnedPersonalization> {
        val actor = actorProvider.requireActor()
        val accountId = resolveReadableAccountId(actor.userId)

        return ownershipReader.findOwned(
            userId = actor.userId,
            accountId = accountId,
            type = query.type,
            page = query.page,
        )
    }

    private fun resolveReadableAccountId(userId: UserId): AccountId? =
        try {
            val accountId = accountAccess.requireActiveAccount(userId)
            val canView =
                accountAccess.hasPermission(
                    accountId = accountId,
                    userId = userId,
                    permission = RolePermission.VIEW_PERSONALIZATIONS,
                )

            accountId.takeIf { canView }
        } catch (_: ActiveAccountRequiredException) {
            null
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

    private fun requirePersonalizationForUpdate(personalizationId: PersonalizationId): Personalization =
        personalizationRepository.findByIdForUpdate(personalizationId)
            ?: throw PersonalizationNotFoundException()

    private fun canCreatorClaim(
        personalization: Personalization,
        userId: UserId,
    ): Boolean =
        personalization.creatorUserId == userId &&
            personalization.visibility == PersonalizationVisibility.PUBLIC &&
            personalization.status == PersonalizationStatus.PUBLISHED

    private fun saveOwnership(ownership: OwnedPersonalization): OwnedPersonalization =
        try {
            ownershipRepository.save(ownership)
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isOwnershipConflict()) {
                throw PersonalizationOwnershipConflictException()
            }

            throw exception
        }

    private fun DataIntegrityViolationException.isOwnershipConflict(): Boolean =
        generateSequence<Throwable>(this) { throwable -> throwable.cause }
            .mapNotNull(Throwable::message)
            .any { message ->
                OWNER_ITEM_CONSTRAINT in message || ACTIVE_SLOT_CONSTRAINT in message
            }

    private companion object {
        const val OWNER_ITEM_CONSTRAINT = "uq_owned_personalizations_owner_item"
        const val ACTIVE_SLOT_CONSTRAINT = "uq_owned_personalizations_active_slot"
    }
}
