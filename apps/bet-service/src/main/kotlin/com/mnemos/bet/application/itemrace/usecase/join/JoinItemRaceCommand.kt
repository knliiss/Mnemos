package com.mnemos.bet.application.itemrace.usecase.join

import com.mnemos.bet.domain.itemrace.model.ItemRaceId

data class JoinItemRaceCommand(
    val raceId: ItemRaceId,
)
