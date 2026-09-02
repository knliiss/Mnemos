package com.mnemos.telegram.bot.contracts.internal.subscription

import java.time.Instant
import java.util.UUID

data class TelegramSubscriptionListResponse(
    val items: List<TelegramSubscriptionResponse>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
    val hasNextPage: Boolean,
)

data class TelegramSubscriptionResponse(
    val id: UUID,
    val type: TelegramSubscriptionType,
    val createdAt: Instant,
    val droppedFor: String? = null,
    val itemName: String? = null,
    val boosterType: String? = null,
    val dungeonType: String? = null,
    val globalType: String? = null,
    val raidLocations: Set<Int> = emptySet(),
)

enum class TelegramSubscriptionType {
    ITEM_DROP,
    BOOSTER,
    DUNGEON,
    RAID,
    GLOBAL,
}
