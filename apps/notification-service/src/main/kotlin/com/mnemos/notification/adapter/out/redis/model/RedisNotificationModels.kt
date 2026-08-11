package com.mnemos.notification.adapter.out.redis.model

import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.policy.EventPriority
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import java.time.Instant
import java.util.UUID

data class RedisNotificationBatch(
    val sourceId: UUID,
    val recipientIds: List<UUID>,
    val type: NotificationType,
    val payload: String,
    val occurredAt: Instant,
    val expiresAt: Instant,
)

data class RedisNotificationItem(
    val sourceId: UUID,
    val type: NotificationType,
    val payload: String,
    val occurredAt: Instant,
    val expiresAt: Instant,
)

data class RedisDeliveryCandidate(
    val id: UUID,
    val userId: UUID,
    val telegramId: Long,
    val eventPriority: EventPriority,
    val recipientPriority: RecipientPriority,
    val readyAt: Instant,
    val items: List<RedisNotificationItem>,
)
