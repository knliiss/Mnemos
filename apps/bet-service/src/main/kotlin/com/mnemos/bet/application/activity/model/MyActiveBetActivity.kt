package com.mnemos.bet.application.activity.model

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.domain.wallet.model.WalletOperation

data class MyActiveBetActivity(
    val eventBets: List<ActiveEventBet>,
    val itemRaces: List<ActiveItemRace>,
    val pendingOperations: List<WalletOperation>,
) {
    data class ActiveEventBet(
        val round: EventBetRoundView,
        val bet: EventBetView,
    )

    data class ActiveItemRace(
        val race: ItemRaceView,
        val participant: ItemRaceParticipantView,
    )
}
