package com.mnemos.profile.adapter.inbound.scheduling.profile

import com.mnemos.profile.application.profile.usecase.reconcile.ReconcilePendingSocialSummariesUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PendingSocialSummaryReconciliationScheduler(
    private val reconcilePendingSocialSummariesUseCase: ReconcilePendingSocialSummariesUseCase,
) {
    @Scheduled(fixedDelayString = "\${mnemos.profile.social-reconciliation-delay-ms:5000}")
    fun reconcile() {
        reconcilePendingSocialSummariesUseCase.execute(BATCH_SIZE)
    }

    private companion object {
        const val BATCH_SIZE = 100
    }
}
