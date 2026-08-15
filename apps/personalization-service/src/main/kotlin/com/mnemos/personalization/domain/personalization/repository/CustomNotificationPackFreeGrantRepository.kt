package com.mnemos.personalization.domain.personalization.repository

import com.mnemos.common.core.model.id.UserId
import java.time.Instant

interface CustomNotificationPackFreeGrantRepository {
    fun claim(
        userId: UserId,
        claimedAt: Instant,
    ): Boolean
}
