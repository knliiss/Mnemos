package com.mnemos.telegram.bot.application.pack.model

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import java.util.UUID

enum class NotificationPackMenuMode {
    STORE,
    OWNED,
    DETAIL,
}

enum class NotificationPackMenuFeedback {
    PURCHASE_ACCEPTED,
    PURCHASE_REFRESH,
    PURCHASE_UNAVAILABLE,
    APPLIED,
    APPLY_REFRESH,
    APPLY_UNAVAILABLE,
}

data class NotificationPackMenuScreenData(
    val mode: NotificationPackMenuMode,
    val store: NotificationPackStoreResult?,
    val detail: NotificationPackDetailResult?,
    val owned: OwnedNotificationPacksResult?,
    val selectedPersonalizationId: UUID?,
    val selectedTemplateType: NotificationPackTemplateType = NotificationPackTemplateType.ITEM_DROP,
    val selectedVariantId: String? = null,
    val feedback: NotificationPackMenuFeedback? = null,
)
