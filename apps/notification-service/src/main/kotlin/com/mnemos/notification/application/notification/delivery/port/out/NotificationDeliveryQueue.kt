package com.mnemos.notification.application.notification.delivery.port.out

import com.mnemos.notification.application.notification.delivery.model.ClaimedDeliveryCandidate
import com.mnemos.notification.application.notification.delivery.model.DeliveryPriority
import java.time.Instant

interface NotificationDeliveryQueue {
    fun claim(
        priority: DeliveryPriority,
        now: Instant,
    ): ClaimedDeliveryCandidate?

    fun complete(candidate: ClaimedDeliveryCandidate)

    fun release(
        candidate: ClaimedDeliveryCandidate,
        retryAt: Instant,
    )

    fun recoverExpired(
        now: Instant,
        limit: Int,
    ): Int
}
