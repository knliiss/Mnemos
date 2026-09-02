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
