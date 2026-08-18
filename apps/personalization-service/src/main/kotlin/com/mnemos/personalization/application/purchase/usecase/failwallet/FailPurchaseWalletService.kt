package com.mnemos.personalization.application.purchase.usecase.failwallet

import com.mnemos.personalization.application.purchase.event.PersonalizationPurchaseEventPublisher
import com.mnemos.personalization.application.purchase.support.PurchaseWalletResultValidator
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.domain.purchase.model.PurchaseOperationStatus
import com.mnemos.personalization.domain.purchase.repository.PurchaseOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FailPurchaseWalletService(
    private val operationRepository: PurchaseOperationRepository,
    private val validator: PurchaseWalletResultValidator,
    private val eventPublisher: PersonalizationPurchaseEventPublisher,
) : FailPurchaseWalletUseCase {
    @Transactional
    override fun execute(
        result: WalletResultContext,
        reason: String,
    ) {
        val operation = operationRepository.findByRequestIdForUpdate(result.requestId) ?: return

        if (operation.status != PurchaseOperationStatus.PENDING) {
            return
        }

        validator.validate(operation, result)

        operation.fail(reason)
        operationRepository.save(operation)

        eventPublisher.publish(operation)
    }
}
