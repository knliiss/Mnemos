package com.mnemos.bet.application.itemrace.usecase.join

import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import java.util.UUID

data class JoinItemRaceCommand(
    val raceId: ItemRaceId,
    val idempotencyKey: UUID? = null,
)
