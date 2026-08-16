package com.mnemos.personalization.application.revenue.usecase.failwallet

import com.mnemos.personalization.application.revenue.support.CreatorRevenueWalletResultValidator
import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class FailCreatorRevenueWalletService(
    private val repository: CreatorRevenueOperationRepository,
    private val validator: CreatorRevenueWalletResultValidator,
    private val properties: CreatorRevenueProperties,
    private val clock: Clock,
) : FailCreatorRevenueWalletUseCase {
    @Transactional
    override fun execute(
        result: WalletResultContext,
        reason: String,
    ) {
        val operation = repository.findByRequestIdForUpdate(result.requestId) ?: return

        if (operation.status != CreatorRevenueOperationStatus.PENDING) {
            return
        }

        validator.validate(operation, result)

        val now = clock.instant()

        operation.fail(
            reason = reason,
            retryAt = now.plus(properties.revenueRetryDelay),
        )
        repository.save(operation)
    }
}
