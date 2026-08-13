package com.mnemos.bet.application.itemrace.usecase.get

interface GetItemRaceUseCase {
    fun execute(query: GetItemRaceQuery): GetItemRaceResult
}
