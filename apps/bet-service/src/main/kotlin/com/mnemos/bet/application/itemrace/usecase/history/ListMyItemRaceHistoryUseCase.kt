package com.mnemos.bet.application.itemrace.usecase.history

import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView

interface ListMyItemRaceHistoryUseCase {
    fun execute(): List<ItemRaceParticipantView>
}
