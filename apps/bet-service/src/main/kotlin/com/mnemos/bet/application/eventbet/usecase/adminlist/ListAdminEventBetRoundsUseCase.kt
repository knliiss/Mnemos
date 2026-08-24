package com.mnemos.bet.application.eventbet.usecase.adminlist

import com.mnemos.bet.application.eventbet.usecase.list.ListEventBetRoundResult

interface ListAdminEventBetRoundsUseCase {
    fun execute(): List<ListEventBetRoundResult>
}
