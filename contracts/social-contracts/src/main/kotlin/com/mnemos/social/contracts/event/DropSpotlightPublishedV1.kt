package com.mnemos.social.contracts.event

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import java.time.Instant
import java.util.UUID

data class DropSpotlightPublishedV1(
    val spotlightId: UUID,
    val sequence: Long,
    val sourceAccountId: UUID,
    val itemDropEventId: UUID,
    val droppedFor: String,
    val itemId: String,
    val itemName: String,
    val itemAmount: Int,
    val luckAmount: Long,
    val notificationPack: NotificationPackSnapshotV1?,
    val publishedAt: Instant,
    val expiresAt: Instant,
    val minimumPresentationGapSeconds: Long,
)
