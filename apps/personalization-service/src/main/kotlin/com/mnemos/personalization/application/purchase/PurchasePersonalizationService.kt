package com.mnemos.personalization.application.purchase

import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.personalization.application.account.port.out.PersonalizationAccountAccess
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.application.purchase.exception.PersonalizationAlreadyOwnedException
import com.mnemos.personalization.application.purchase.exception.PersonalizationNotPurchasableException
import com.mnemos.personalization.application.purchase.exception.PersonalizationPurchaseInProgressException
import com.mnemos.personalization.application.purchase.port.out.PersonalizationWallet
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationId
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class PurchasePersonalizationResult(
    val personalizationId: PersonalizationId,
    val operationId: PurchaseOperationId,
)

@Service
class PurchasePersonalizationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val accountAccess: PersonalizationAccountAccess,
    private val personalizationRepository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val purchaseOperationRepository: PurchaseOperationRepository,
    private val wallet: PersonalizationWallet,
    private val clock: Clock,
) {
    @Transactional
    fun execute(personalizationId: PersonalizationId): PurchasePersonalizationResult {
        val actor = actorProvider.requireActor()
        val personalization =
            personalizationRepository.findById(personalizationId)
                ?: throw PersonalizationNotFoundException()

        if (!personalization.isPurchasableBy(actor.userId)) {
            throw PersonalizationNotPurchasableException()
        }

        val owner =
            when (personalization.type.target) {
                PersonalizationTarget.USER ->
                    PersonalizationOwner.User(actor.userId)

                PersonalizationTarget.ACCOUNT -> {
                    val accountId = accountAccess.requireActiveAccount(actor.userId)
                    val granted =
                        accountAccess.hasPermission(
                            accountId = accountId,
                            userId = actor.userId,
                            permission = RolePermission.BUY_PERSONALIZATIONS,
                        )

                    if (!granted) {
                        throw PersonalizationAccessDeniedException()
                    }

                    PersonalizationOwner.Account(accountId)
                }
            }

        if (ownershipRepository.exists(owner, personalization.id)) {
            throw PersonalizationAlreadyOwnedException()
        }

        if (purchaseOperationRepository.existsPending(owner, personalization.id)) {
            throw PersonalizationPurchaseInProgressException()
        }

        val price = personalization.price ?: throw PersonalizationNotPurchasableException()
        val operation =
            PurchaseOperation.create(
                personalizationId = personalization.id,
                buyerUserId = actor.userId,
                owner = owner,
                currency = price.currency,
                amount = price.amount,
                now = clock.instant(),
            )

        val saved = purchaseOperationRepository.save(operation)

        wallet.requestDebit(saved)

        return PurchasePersonalizationResult(
            personalizationId = personalization.id,
            operationId = saved.id,
        )
    }
}
