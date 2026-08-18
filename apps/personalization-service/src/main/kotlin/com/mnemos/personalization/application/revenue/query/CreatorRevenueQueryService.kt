package com.mnemos.personalization.application.revenue.query

import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CreatorRevenueQueryService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: CreatorRevenueOperationRepository,
) {
    fun getSummary(): CreatorRevenueSummary {
        val operations = getOperations()

        return CreatorRevenueSummary(
            confirmedPrime = operations.sumAmount(CreatorRevenueOperationStatus.CONFIRMED),
            pendingPrime = operations.sumAmount(CreatorRevenueOperationStatus.PENDING),
            failedPrime = operations.sumAmount(CreatorRevenueOperationStatus.FAILED),
            confirmedOperations = operations.countStatus(CreatorRevenueOperationStatus.CONFIRMED),
            pendingOperations = operations.countStatus(CreatorRevenueOperationStatus.PENDING),
            failedOperations = operations.countStatus(CreatorRevenueOperationStatus.FAILED),
        )
    }

    fun getOperations(): List<CreatorRevenueOperation> {
        val actor = actorProvider.requireActor()

        return repository.findByCreatorUserId(actor.userId)
    }

    private fun List<CreatorRevenueOperation>.sumAmount(status: CreatorRevenueOperationStatus): Long =
        asSequence()
            .filter { operation -> operation.status == status }
            .sumOf(CreatorRevenueOperation::amountPrime)

    private fun List<CreatorRevenueOperation>.countStatus(status: CreatorRevenueOperationStatus): Int =
        count { operation -> operation.status == status }
}
