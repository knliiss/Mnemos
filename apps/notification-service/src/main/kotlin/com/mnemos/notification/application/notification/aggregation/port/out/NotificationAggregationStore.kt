package com.mnemos.notification.application.notification.aggregation.port.out

import com.mnemos.notification.application.notification.aggregation.model.ClaimedNotificationAggregation
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidate
import com.mnemos.notification.application.notification.model.NotificationBatch
import java.time.Instant

interface NotificationAggregationStore {
    fun add(
        batch: NotificationBatch,
        flushAt: Instant,
    ): Boolean

    fun claimDue(
        now: Instant,
        limit: Int,
    ): List<ClaimedNotificationAggregation>

    fun complete(aggregation: ClaimedNotificationAggregation)

    fun release(
        aggregation: ClaimedNotificationAggregation,
        retryAt: Instant,
    )

    fun recoverExpired(
        now: Instant,
        limit: Int,
    ): Int

    fun handoff(
        aggregation: ClaimedNotificationAggregation,
        candidate: DeliveryCandidate,
    ): Boolean
}
