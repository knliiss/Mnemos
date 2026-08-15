package com.mnemos.personalization.adapter.inbound.scheduling.revenue

import com.mnemos.personalization.application.revenue.RetryCreatorRevenueService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CreatorRevenueRetryScheduler(
    private val retryCreatorRevenueService: RetryCreatorRevenueService,
) {
    @Scheduled(fixedDelayString = "\${mnemos.personalization.creator.retry-scan-delay-ms:10000}")
    fun retryDueRevenue() {
        retryCreatorRevenueService.execute()
    }
}
