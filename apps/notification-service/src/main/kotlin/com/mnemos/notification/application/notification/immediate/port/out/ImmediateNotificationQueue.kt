package com.mnemos.notification.application.notification.immediate.port.out

import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidate
import com.mnemos.notification.application.notification.immediate.model.ClaimedImmediateNotification
import com.mnemos.notification.application.notification.model.NotificationBatch
import java.time.Instant

interface ImmediateNotificationQueue {
    fun enqueue(
        batch: NotificationBatch,
        availableAt: Instant,
    ): Boolean

    fun claimDue(
        now: Instant,
        limit: Int,
    ): List<ClaimedImmediateNotification>

    fun complete(notification: ClaimedImmediateNotification)

    fun release(
        notification: ClaimedImmediateNotification,
        retryAt: Instant,
    )

    fun recoverExpired(
        now: Instant,
        limit: Int,
    ): Int

    fun handoff(
        notification: ClaimedImmediateNotification,
        candidates: List<DeliveryCandidate>,
    ): Boolean
}
