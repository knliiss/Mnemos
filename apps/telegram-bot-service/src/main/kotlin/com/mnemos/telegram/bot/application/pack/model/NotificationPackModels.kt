package com.mnemos.telegram.bot.application.pack.model

import java.util.UUID

data class TelegramNotificationPackStorePage(
    val packs: List<TelegramNotificationPackStoreItem>,
    val page: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)

data class TelegramNotificationPackStoreItem(
    val personalizationId: UUID,
    val name: String,
    val author: String,
    val priceAmount: Long,
    val currency: String,
    val owned: Boolean,
    val active: Boolean,
    val samples: List<String>,
)

data class TelegramOwnedNotificationPack(
    val personalizationId: UUID,
    val name: String,
    val active: Boolean,
)

sealed interface NotificationPackStoreResult {
    data class Available(
        val page: TelegramNotificationPackStorePage,
    ) : NotificationPackStoreResult

    data object RegistrationRequired : NotificationPackStoreResult

    data object Unavailable : NotificationPackStoreResult
}

sealed interface OwnedNotificationPacksResult {
    data class Available(
        val packs: List<TelegramOwnedNotificationPack>,
    ) : OwnedNotificationPacksResult

    data object RegistrationRequired : OwnedNotificationPacksResult

    data object Unavailable : OwnedNotificationPacksResult
}

sealed interface NotificationPackPurchaseResult {
    data class Accepted(
        val operationId: UUID,
    ) : NotificationPackPurchaseResult

    data object Refresh : NotificationPackPurchaseResult

    data object Unavailable : NotificationPackPurchaseResult
}

sealed interface NotificationPackApplyResult {
    data object Applied : NotificationPackApplyResult

    data object Refresh : NotificationPackApplyResult

    data object Unavailable : NotificationPackApplyResult
}
