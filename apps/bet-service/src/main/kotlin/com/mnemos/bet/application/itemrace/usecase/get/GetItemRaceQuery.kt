package com.mnemos.bet.application.itemrace.usecase.get

import com.mnemos.bet.domain.itemrace.model.ItemRaceId

data class GetItemRaceQuery(
    val raceId: ItemRaceId,
)
