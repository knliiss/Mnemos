package com.mnemos.personalization.contracts.internal.v1

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
    val samples: List<String>,
)

data class PurchaseInternalTelegramNotificationPackRequest(
    val idempotencyKey: UUID,
)

data class InternalTelegramNotificationPackPurchaseResponse(
    val personalizationId: UUID,
    val operationId: UUID,
)
