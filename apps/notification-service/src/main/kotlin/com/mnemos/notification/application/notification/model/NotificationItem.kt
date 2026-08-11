package com.mnemos.notification.application.notification.model

import java.time.Instant
import java.util.UUID

data class NotificationItem(
    val sourceId: UUID,
    val payload: NotificationPayload,
    val occurredAt: Instant,
    val expiresAt: Instant,
)
