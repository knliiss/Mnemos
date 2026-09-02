package com.mnemos.personalization.contracts.internal.v1

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import java.util.UUID

data class InternalTelegramNotificationPackListResponse(
    val packs: List<InternalTelegramNotificationPackSummaryResponse>,
)

data class InternalTelegramNotificationPackSummaryResponse(
    val personalizationId: UUID,
    val name: String,
    val active: Boolean,
)

data class UpdateInternalTelegramNotificationPackSelectionRequest(
    val personalizationId: UUID,
)

data class InternalTelegramNotificationPackStoreResponse(
    val packs: List<InternalTelegramNotificationPackStoreItemResponse>,
    val page: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNextPage: Boolean,
)

data class InternalTelegramNotificationPackStoreItemResponse(
    val personalizationId: UUID,
    val name: String,
    val author: String,
    val priceAmount: Long,
    val currency: String,
    val owned: Boolean,
    val active: Boolean,
)

data class InternalTelegramNotificationPackDetailResponse(
    val personalizationId: UUID,
    val name: String,
    val author: String,
    val priceAmount: Long,
    val currency: String,
    val owned: Boolean,
    val active: Boolean,
    val preview: InternalTelegramNotificationPackPreviewResponse,
)

data class InternalTelegramNotificationPackPreviewResponse(
    val mixedAggregateTitle: String,
    val templates: Map<NotificationPackTemplateType, InternalTelegramNotificationTemplateResponse>,
    val valueMappings: Map<NotificationPackValueKey, Map<String, InternalTelegramNotificationValueFormsResponse>>,
)

data class InternalTelegramNotificationTemplateResponse(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
    val variants: List<InternalTelegramNotificationTemplateVariantResponse>,
)

data class InternalTelegramNotificationTemplateVariantResponse(
    val id: String,
    val conditions: List<InternalTelegramNotificationTemplateConditionResponse>,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)

data class InternalTelegramNotificationTemplateConditionResponse(
    val field: NotificationPackVariantField,
    val value: String,
)

data class InternalTelegramNotificationValueFormsResponse(
    val default: String,
    val forms: Map<String, String>,
)

data class PurchaseInternalTelegramNotificationPackRequest(
    val idempotencyKey: UUID,
)

data class InternalTelegramNotificationPackPurchaseResponse(
    val personalizationId: UUID,
    val operationId: UUID,
)
