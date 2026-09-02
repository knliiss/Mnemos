package com.mnemos.bet.adapter.inbound.http.internal

import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetApiMapper.toResponse
import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetResponse
import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetRoundResponse
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.usecase.list.ListEventBetRoundResult
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramBetSide
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetRoundResponse

object TelegramEventBetInternalApiMapper {
    fun ListEventBetRoundResult.toTelegramResponse(): TelegramEventBetRoundResponse = toResponse().toTelegramResponse()

    fun EventBetView.toTelegramResponse(): TelegramEventBetResponse = toResponse().toTelegramResponse()

    private fun EventBetRoundResponse.toTelegramResponse(): TelegramEventBetRoundResponse =
        TelegramEventBetRoundResponse(
            id = id,
            eventType = eventType.name,
            scheduledAt = scheduledAt,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            yesOdds = yesOdds,
            noOdds = noOdds,
            status = status.name,
            outcome = outcome?.name,
            myBet = myBet?.toTelegramResponse(),
        )

    private fun EventBetResponse.toTelegramResponse(): TelegramEventBetResponse =
        TelegramEventBetResponse(
            id = id,
            roundId = roundId,
            side = TelegramBetSide.valueOf(side.name),
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status.name,
        )
}
