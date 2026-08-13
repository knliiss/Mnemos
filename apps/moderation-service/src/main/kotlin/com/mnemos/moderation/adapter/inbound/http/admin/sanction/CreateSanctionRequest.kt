package com.mnemos.moderation.adapter.inbound.http.admin.sanction

import com.mnemos.moderation.contracts.shared.SanctionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.util.UUID

data class CreateSanctionRequest(
    val userId: UUID,
    val type: SanctionType,
    @field:NotBlank
    val reason: String,
    val internalNote: String? = null,
    @field:Positive
    val durationHours: Long? = null,
)
