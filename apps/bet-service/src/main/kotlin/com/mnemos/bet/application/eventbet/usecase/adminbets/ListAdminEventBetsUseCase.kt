package com.mnemos.bet.application.eventbet.usecase.adminbets

import com.mnemos.bet.application.eventbet.model.AdminEventBetView
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId

interface ListAdminEventBetsUseCase {
    fun execute(roundId: EventBetRoundId): List<AdminEventBetView>
}
