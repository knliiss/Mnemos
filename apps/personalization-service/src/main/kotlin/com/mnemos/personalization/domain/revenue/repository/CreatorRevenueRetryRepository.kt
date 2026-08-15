package com.mnemos.personalization.domain.revenue.repository

import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import java.time.Instant

interface CreatorRevenueRetryRepository {
    fun findDueForRetryForUpdate(
        now: Instant,
        limit: Int,
    ): List<CreatorRevenueOperation>
}
