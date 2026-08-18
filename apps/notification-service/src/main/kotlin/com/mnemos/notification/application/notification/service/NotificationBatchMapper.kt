package com.mnemos.notification.application.notification.service

import com.mnemos.notification.application.notification.model.NotificationBatch
import com.mnemos.notification.application.notification.model.NotificationItem

object NotificationBatchMapper {
    fun NotificationBatch.toItem(): NotificationItem =
        NotificationItem(
            sourceId = sourceId,
            payload = payload,
            occurredAt = occurredAt,
            expiresAt = expiresAt,
        )
}
