package com.mnemos.personalization.adapter.inbound.scheduling.revenue

import com.mnemos.personalization.application.revenue.usecase.retry.RetryCreatorRevenueUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CreatorRevenueRetryScheduler(
    private val retryCreatorRevenueUseCase: RetryCreatorRevenueUseCase,
) {
    @Scheduled(fixedDelayString = "\${mnemos.personalization.creator.retry-scan-delay-ms:10000}")
    fun retryDueRevenue() {
        retryCreatorRevenueUseCase.execute()
    }
}
