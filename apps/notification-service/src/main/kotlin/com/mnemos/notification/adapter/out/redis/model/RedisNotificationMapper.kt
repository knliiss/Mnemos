package com.mnemos.notification.adapter.out.redis.model

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidate
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidateId
import com.mnemos.notification.application.notification.delivery.model.DeliveryPriority
import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.DungeonNotificationPayload
import com.mnemos.notification.application.notification.model.GlobalNotificationPayload
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationBatch
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationPayload
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.model.PlayerOfflineNotificationPayload
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.recipient.model.RecipientView
import tools.jackson.databind.json.JsonMapper

object RedisNotificationMapper {
    fun NotificationBatch.toRedis(jsonMapper: JsonMapper): RedisNotificationBatch =
        RedisNotificationBatch(
            sourceId = sourceId,
            recipientIds = recipientIds.map { it.id }.sorted(),
            type = payload.type,
            payload = jsonMapper.writeValueAsString(payload),
            occurredAt = occurredAt,
            expiresAt = expiresAt,
        )

    fun RedisNotificationBatch.toDomain(jsonMapper: JsonMapper): NotificationBatch =
        NotificationBatch(
            sourceId = sourceId,
            recipientIds = recipientIds.mapTo(HashSet(), ::UserId),
            payload = readPayload(type, payload, jsonMapper),
            occurredAt = occurredAt,
            expiresAt = expiresAt,
        )

    fun NotificationItem.toRedis(jsonMapper: JsonMapper): RedisNotificationItem =
        RedisNotificationItem(
            sourceId = sourceId,
            type = payload.type,
            payload = jsonMapper.writeValueAsString(payload),
            occurredAt = occurredAt,
            expiresAt = expiresAt,
        )

    fun RedisNotificationItem.toDomain(jsonMapper: JsonMapper): NotificationItem =
        NotificationItem(
            sourceId = sourceId,
            payload = readPayload(type, payload, jsonMapper),
            occurredAt = occurredAt,
            expiresAt = expiresAt,
        )

    fun DeliveryCandidate.toRedis(jsonMapper: JsonMapper): RedisDeliveryCandidate =
        RedisDeliveryCandidate(
            id = id.id,
            userId = recipient.userId.id,
            telegramId = recipient.telegramId.id,
            eventPriority = priority.event,
            recipientPriority = priority.recipient,
            readyAt = readyAt,
            items = items.map { it.toRedis(jsonMapper) },
        )

    fun RedisDeliveryCandidate.toDomain(jsonMapper: JsonMapper): DeliveryCandidate =
        DeliveryCandidate(
            id = DeliveryCandidateId(id),
            recipient =
                RecipientView(
                    userId = UserId(userId),
                    telegramId = TelegramId(telegramId),
                    priority = recipientPriority,
                ),
            items = items.map { it.toDomain(jsonMapper) },
            priority = DeliveryPriority(eventPriority, recipientPriority),
            readyAt = readyAt,
        )

    private fun readPayload(
        type: NotificationType,
        payload: String,
        jsonMapper: JsonMapper,
    ): NotificationPayload =
        when (type) {
            NotificationType.ITEM_DROP -> jsonMapper.readValue(payload, ItemDropNotificationPayload::class.java)
            NotificationType.BOOSTER -> jsonMapper.readValue(payload, BoosterNotificationPayload::class.java)
            NotificationType.DUNGEON -> jsonMapper.readValue(payload, DungeonNotificationPayload::class.java)
            NotificationType.RAID -> jsonMapper.readValue(payload, RaidNotificationPayload::class.java)
            NotificationType.GLOBAL -> jsonMapper.readValue(payload, GlobalNotificationPayload::class.java)
            NotificationType.PLAYER_OFFLINE -> jsonMapper.readValue(payload, PlayerOfflineNotificationPayload::class.java)
        }
}
