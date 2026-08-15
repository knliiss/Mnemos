package com.mnemos.personalization.application.purchase

import com.mnemos.personalization.application.purchase.event.PersonalizationPurchaseEventPublisher
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.repository.OwnedPersonalizationRepository
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
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
        requestId: UUID,
        transactionId: UUID,
    ) {
        val operation =
            operationRepository.findByRequestIdForUpdate(requestId)
                ?: return

        if (operation.status != PurchaseOperationStatus.PENDING) {
            return
        }

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
        requestId: UUID,
        reason: String,
    ) {
        val operation =
            operationRepository.findByRequestIdForUpdate(requestId)
                ?: return

        if (operation.status != PurchaseOperationStatus.PENDING) {
            return
        }

        operation.fail(reason)
        operationRepository.save(operation)

        eventPublisher.publish(operation)
    }
}
