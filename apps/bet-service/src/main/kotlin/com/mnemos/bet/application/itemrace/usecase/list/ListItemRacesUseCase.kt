package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.itemrace.model.ItemRaceView

interface ListItemRacesUseCase {
    fun execute(): List<ItemRaceView>
}
