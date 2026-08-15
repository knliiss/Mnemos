package com.mnemos.profile.adapter.inbound.scheduling.profile

import com.mnemos.profile.application.profile.PublicProfileService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PendingSocialSummaryReconciliationScheduler(
    private val service: PublicProfileService,
) {
    @Scheduled(fixedDelayString = "\${mnemos.profile.social-reconciliation-delay-ms:5000}")
    fun reconcile() {
        service.reconcilePendingSocialSummaries(BATCH_SIZE)
    }

    private companion object {
        const val BATCH_SIZE = 100
    }
}
