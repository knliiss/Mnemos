package com.mnemos.telegram.bot.application.pack.model

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
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
)

data class TelegramNotificationPackDetail(
    val personalizationId: UUID,
    val name: String,
    val author: String,
    val priceAmount: Long,
    val currency: String,
    val owned: Boolean,
    val active: Boolean,
    val preview: TelegramNotificationPackPreview,
)

data class TelegramNotificationPackPreview(
    val mixedAggregateTitle: String,
    val templates: Map<NotificationPackTemplateType, TelegramNotificationTemplate>,
    val valueMappings: Map<NotificationPackValueKey, Map<String, TelegramNotificationValueForms>>,
)

data class TelegramNotificationTemplate(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
    val variants: List<TelegramNotificationTemplateVariant>,
)

data class TelegramNotificationTemplateVariant(
    val id: String,
    val conditions: List<TelegramNotificationTemplateCondition>,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)

data class TelegramNotificationTemplateCondition(
    val field: NotificationPackVariantField,
    val value: String,
)

data class TelegramNotificationValueForms(
    val default: String,
    val forms: Map<String, String>,
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

sealed interface NotificationPackDetailResult {
    data class Available(
        val pack: TelegramNotificationPackDetail,
    ) : NotificationPackDetailResult

    data object NotFound : NotificationPackDetailResult

    data object Unavailable : NotificationPackDetailResult
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
