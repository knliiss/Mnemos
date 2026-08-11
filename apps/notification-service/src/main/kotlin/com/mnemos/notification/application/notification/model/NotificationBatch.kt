package com.mnemos.notification.application.notification.model

import com.mnemos.common.core.model.id.UserId
import java.time.Instant
import java.util.UUID

data class NotificationBatch(
    val sourceId: UUID,
    val recipientIds: Set<UserId>,
    val payload: NotificationPayload,
    val occurredAt: Instant,
    val expiresAt: Instant,
) {
    init {
        require(recipientIds.isNotEmpty())
    }
}
