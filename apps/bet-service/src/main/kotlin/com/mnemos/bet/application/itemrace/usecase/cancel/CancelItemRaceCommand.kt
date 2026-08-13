package com.mnemos.bet.application.itemrace.usecase.cancel

import com.mnemos.bet.domain.itemrace.model.ItemRaceId

data class CancelItemRaceCommand(
    val raceId: ItemRaceId,
)
