package com.mnemos.bet.adapter.inbound.http.admin.itemrace

import com.mnemos.bet.contracts.shared.BetItemType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateItemRaceRequest(
    @field:NotBlank
    @field:Size(max = 24)
    val itemName: String,
    val itemType: BetItemType,
    @field:Min(1)
    @field:Max(168)
    val durationHours: Long = 24,
    @field:Min(1000)
    @field:Max(5000)
    val entryAmount: Long,
)
