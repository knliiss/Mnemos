package com.mnemos.profile.application.profile.usecase.reconcile

import com.mnemos.profile.application.profile.port.out.PendingSocialSummaryRepository
import com.mnemos.profile.application.profile.support.PendingSocialSummaryCoordinator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReconcilePendingSocialSummariesService(
    private val pendingRepository: PendingSocialSummaryRepository,
    private val pendingCoordinator: PendingSocialSummaryCoordinator,
) : ReconcilePendingSocialSummariesUseCase {
    @Transactional
    override fun execute(limit: Int) {
        require(limit > 0) {
            "Pending social summary reconciliation limit must be positive"
        }

        pendingRepository
            .findReady(limit)
            .forEach { summary ->
                pendingCoordinator.apply(summary.accountId)
            }
    }
}
