package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult

interface ListItemRacesUseCase {
    fun execute(): List<GetItemRaceResult>
}
