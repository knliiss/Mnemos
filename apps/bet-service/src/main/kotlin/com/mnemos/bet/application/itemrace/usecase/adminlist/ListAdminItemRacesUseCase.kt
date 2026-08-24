package com.mnemos.bet.application.itemrace.usecase.adminlist

import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult

interface ListAdminItemRacesUseCase {
    fun execute(): List<GetItemRaceResult>
}
