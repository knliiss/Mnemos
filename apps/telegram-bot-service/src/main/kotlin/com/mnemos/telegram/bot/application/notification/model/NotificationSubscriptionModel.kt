package com.mnemos.telegram.bot.application.notification.model

import java.time.Instant
import java.util.UUID

enum class NotificationSubscriptionType {
    ITEM_DROP,
    BOOSTER,
    DUNGEON,
    RAID,
    GLOBAL,
}

data class NotificationSubscription(
    val id: UUID,
    val type: NotificationSubscriptionType,
    val createdAt: Instant,
    val droppedFor: String?,
    val itemName: String?,
    val boosterType: String?,
    val dungeonType: String?,
    val globalType: String?,
    val raidLocations: Set<Int>,
)

data class NotificationSubscriptionPage(
    val items: List<NotificationSubscription>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
    val hasNextPage: Boolean,
)

sealed interface NotificationSubscriptionResult {
    data class Available(
        val page: NotificationSubscriptionPage,
        val boosterSubscriptions: Map<NotificationBoosterType, UUID> = emptyMap(),
        val dungeonSubscriptions: Map<NotificationDungeonType, UUID> = emptyMap(),
        val globalSubscriptions: Map<NotificationGlobalType, UUID> = emptyMap(),
        val raidSubscription: NotificationSubscription? = null,
        val pingEnabled: Boolean = true,
    ) : NotificationSubscriptionResult

    data object Unavailable : NotificationSubscriptionResult

    data object NotLoaded : NotificationSubscriptionResult
}

data class NotificationMenuScreenData(
    val result: NotificationSubscriptionResult,
    val mode: NotificationCreationMode = NotificationCreationMode.LIST,
    val feedback: NotificationMenuFeedback? = null,
    val itemDropInput: NotificationItemDropInputField? = null,
    val droppedFor: String? = null,
    val itemName: String? = null,
    val deleteSubscriptionId: UUID? = null,
    val raidLocations: Set<Int> = emptySet(),
    val raidPage: Int = 0,
)
