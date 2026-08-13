package com.mnemos.bet.application.itemrace.usecase.get

import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView

data class GetItemRaceResult(
    val race: ItemRaceView,
    val participants: List<ItemRaceParticipantView>,
)
