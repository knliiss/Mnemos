package com.mnemos.personalization.application.revenue

import com.mnemos.personalization.application.revenue.port.out.CreatorRevenueWallet
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueRetryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class RetryCreatorRevenueService(
    private val operationRepository: CreatorRevenueOperationRepository,
    private val retryRepository: CreatorRevenueRetryRepository,
    private val wallet: CreatorRevenueWallet,
    private val properties: CreatorRevenueProperties,
    private val clock: Clock,
) {
    @Transactional
    fun execute(): Int {
        val now = clock.instant()
        val operations =
            retryRepository.findDueForRetryForUpdate(
                now = now,
                limit = properties.revenueRetryBatchSize,
            )

        operations.forEach { operation ->
            operation.prepareRetry(now)
            operationRepository.save(operation)
            wallet.requestCredit(operation)
        }

        return operations.size
    }
}
