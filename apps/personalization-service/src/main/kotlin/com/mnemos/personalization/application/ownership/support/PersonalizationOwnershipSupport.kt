package com.mnemos.personalization.application.ownership.support

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.exception.ActiveAccountRequiredException
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.ownership.exception.PersonalizationOwnershipConflictException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class PersonalizationOwnershipSupport(
    private val accountAccess: PersonalizationAccountAccess,
    private val personalizationRepository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val purchaseOperationRepository: PurchaseOperationRepository,
) {
    fun resolveOwner(
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
                        permission = RolePermission.APPLY_ACCOUNT_PERSONALIZATIONS,
                    )

                if (!granted) {
                    throw PersonalizationAccessDeniedException()
                }

                PersonalizationOwner.Account(accountId)
            }
        }

    fun resolveReadableAccountId(userId: UserId): AccountId? =
        try {
            accountAccess.requireActiveAccount(userId)
        } catch (_: ActiveAccountRequiredException) {
            null
        }

    fun requirePersonalization(personalizationId: PersonalizationId): Personalization =
        personalizationRepository.findById(personalizationId)
            ?: throw PersonalizationNotFoundException()

    fun requirePersonalizationForUpdate(personalizationId: PersonalizationId): Personalization =
        personalizationRepository.findByIdForUpdate(personalizationId)
            ?: throw PersonalizationNotFoundException()

    fun canCreatorClaim(
        personalization: Personalization,
        userId: UserId,
    ): Boolean =
        personalization.creatorUserId == userId &&
            personalization.visibility == PersonalizationVisibility.PUBLIC &&
            personalization.status == PersonalizationStatus.PUBLISHED

    fun hasPendingPurchase(
        owner: PersonalizationOwner,
        personalizationId: PersonalizationId,
    ): Boolean = purchaseOperationRepository.existsPending(owner, personalizationId)

    fun saveOwnership(ownership: OwnedPersonalization): OwnedPersonalization =
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
