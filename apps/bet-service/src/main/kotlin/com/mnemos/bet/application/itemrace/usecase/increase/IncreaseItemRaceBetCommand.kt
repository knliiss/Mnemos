package com.mnemos.bet.application.itemrace.usecase.increase

import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import java.util.UUID

data class IncreaseItemRaceBetCommand(
    val raceId: ItemRaceId,
    val amount: Long,
    val idempotencyKey: UUID? = null,
)
