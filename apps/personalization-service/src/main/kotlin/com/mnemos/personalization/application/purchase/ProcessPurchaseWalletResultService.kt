package com.mnemos.personalization.application.purchase

import com.mnemos.personalization.application.purchase.event.PersonalizationPurchaseEventPublisher
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class ProcessPurchaseWalletResultService(
    private val operationRepository: PurchaseOperationRepository,
    private val personalizationRepository: PersonalizationRepository,
    private val ownershipRepository: OwnedPersonalizationRepository,
    private val eventPublisher: PersonalizationPurchaseEventPublisher,
    private val clock: Clock,
) {
    @Transactional
    fun complete(
        result: WalletResultContext,
        transactionId: UUID,
    ) {
        val operation =
            operationRepository.findByRequestIdForUpdate(result.requestId)
                ?: return

        if (operation.status != PurchaseOperationStatus.PENDING) {
            return
        }

        requireExpectedResult(operation, result)

        val personalization =
            requireNotNull(personalizationRepository.findById(operation.personalizationId)) {
                "Purchased personalization ${operation.personalizationId} no longer exists"
            }

        operation.complete(transactionId)
        operationRepository.save(operation)

        if (!ownershipRepository.exists(operation.owner, operation.personalizationId)) {
            ownershipRepository.save(
                OwnedPersonalization(
                    owner = operation.owner,
                    personalizationId = operation.personalizationId,
                    type = personalization.type,
                    acquiredAt = clock.instant(),
                    active = false,
                ),
            )
        }

        eventPublisher.publish(operation)
    }

    @Transactional
    fun fail(
        result: WalletResultContext,
        reason: String,
    ) {
        val operation =
            operationRepository.findByRequestIdForUpdate(result.requestId)
                ?: return

        if (operation.status != PurchaseOperationStatus.PENDING) {
            return
        }

        requireExpectedResult(operation, result)

        operation.fail(reason)
        operationRepository.save(operation)

        eventPublisher.publish(operation)
    }

    private fun requireExpectedResult(
        operation: PurchaseOperation,
        result: WalletResultContext,
    ) {
        val expectedOwner = PurchaseWalletOwnerResolver.resolve(operation)

        check(result.actorUserId == operation.buyerUserId.id) {
            "Personalization purchase wallet result actor does not match buyer"
        }
        check(result.ownerType == expectedOwner.type && result.ownerId == expectedOwner.id) {
            "Personalization purchase wallet result owner does not match operation payer"
        }
        check(result.currency == operation.currency) {
            "Personalization purchase wallet result currency does not match operation"
        }
        check(result.transactionType == TransactionType.DEBIT) {
            "Personalization purchase wallet result must be a debit"
        }
        check(result.source == TransactionSource.PERSONALIZATION_PURCHASE) {
            "Personalization purchase wallet result source does not match operation"
        }
        check(result.sourceReferenceId == operation.id.id) {
            "Personalization purchase wallet result source reference does not match operation"
        }
        check(result.amount == operation.amount) {
            "Personalization purchase wallet result amount does not match operation"
        }
    }
}
