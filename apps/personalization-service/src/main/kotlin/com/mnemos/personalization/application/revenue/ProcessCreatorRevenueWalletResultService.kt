package com.mnemos.personalization.application.revenue

import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class ProcessCreatorRevenueWalletResultService(
    private val repository: CreatorRevenueOperationRepository,
    private val properties: CreatorRevenueProperties,
    private val clock: Clock,
) {
    @Transactional
    fun complete(
        requestId: UUID,
        transactionId: UUID,
    ) {
        val operation = repository.findByRequestIdForUpdate(requestId) ?: return

        if (operation.status != CreatorRevenueOperationStatus.PENDING) {
            return
        }

        operation.complete(transactionId)
        repository.save(operation)
    }

    @Transactional
    fun fail(
        requestId: UUID,
        reason: String,
    ) {
        val operation = repository.findByRequestIdForUpdate(requestId) ?: return

        if (operation.status != CreatorRevenueOperationStatus.PENDING) {
            return
        }

        val now = clock.instant()

        operation.fail(
            reason = reason,
            retryAt = now.plus(properties.revenueRetryDelay),
        )
        repository.save(operation)
    }
}
