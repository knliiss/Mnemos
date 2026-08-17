package com.mnemos.realtime.application.broadcast.model

import java.time.Instant
import java.util.UUID

data class DropSpotlightBroadcastPayload(
    val spotlightId: UUID,
    val sequence: Long,
    val sourceAccountId: UUID,
    val itemDropEventId: UUID,
    val droppedFor: String,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: String,
    val itemType: String,
    val notificationPack: NotificationPackBroadcastSnapshot?,
    val publishedAt: Instant,
    val expiresAt: Instant,
    val minimumPresentationGapSeconds: Long,
)
