package com.mnemos.personalization.application.revenue.usecase.completewallet

import com.mnemos.personalization.application.revenue.support.CreatorRevenueWalletResultValidator
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CompleteCreatorRevenueWalletService(
    private val repository: CreatorRevenueOperationRepository,
    private val validator: CreatorRevenueWalletResultValidator,
) : CompleteCreatorRevenueWalletUseCase {
    @Transactional
    override fun execute(
        result: WalletResultContext,
        transactionId: UUID,
    ) {
        val operation = repository.findByRequestIdForUpdate(result.requestId) ?: return

        if (operation.status != CreatorRevenueOperationStatus.PENDING) {
            return
        }

        validator.validate(operation, result)

        operation.complete(transactionId)
        repository.save(operation)
    }
}
