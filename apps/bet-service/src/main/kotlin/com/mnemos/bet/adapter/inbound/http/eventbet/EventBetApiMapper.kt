package com.mnemos.bet.adapter.inbound.http.eventbet

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.usecase.get.GetEventBetRoundResult
import com.mnemos.bet.application.eventbet.usecase.list.ListEventBetRoundResult
import com.mnemos.bet.contracts.shared.BetGlobalEventType
import com.mnemos.bet.contracts.shared.BetSide

object EventBetApiMapper {
    fun GetEventBetRoundResult.toResponse(): EventBetRoundResponse = round.toResponse(myBet)

    fun ListEventBetRoundResult.toResponse(): EventBetRoundResponse = round.toResponse(myBet)

    fun EventBetRoundView.toResponse(myBet: EventBetView? = null): EventBetRoundResponse =
        EventBetRoundResponse(
            id = id.id,
            eventType = BetGlobalEventType.valueOf(eventType.name),
            scheduledAt = scheduledAt,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            yesOdds = yesOdds,
            noOdds = noOdds,
            status = status,
            outcome = effectiveOutcome,
            myBet = myBet?.toResponse(),
        )

    fun EventBetView.toResponse(): EventBetResponse =
        EventBetResponse(
            id = id.id,
            roundId = roundId.id,
            side = BetSide.valueOf(side.name),
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
