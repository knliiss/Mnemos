package com.mnemos.telegram.bot.adapter.out.bet

import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBet
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBetRound
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRace
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacePosition
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetRoundResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceParticipantResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceResponse

object BetApiMapper {
    fun TelegramEventBetRoundResponse.toModel(): TelegramEventBetRound =
        TelegramEventBetRound(
            id = id,
            eventType = eventType,
            scheduledAt = scheduledAt,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            yesOdds = yesOdds,
            noOdds = noOdds,
            status = status,
            outcome = outcome,
            myBet = myBet?.toModel(),
        )

    fun TelegramEventBetResponse.toModel(): TelegramEventBet =
        TelegramEventBet(
            id = id,
            roundId = roundId,
            side = TelegramBetSide.valueOf(side.name),
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )

    fun TelegramItemRaceResponse.toModel(): TelegramItemRace =
        TelegramItemRace(
            id = id,
            itemName = itemName,
            itemType = itemType,
            registrationClosesAt = registrationClosesAt,
            startsAt = startsAt,
            expiresAt = expiresAt,
            entryAmount = entryAmount,
            feePercent = feePercent,
            status = status,
            winnerAccountId = winnerAccountId,
            myPosition = myPosition?.toModel(),
        )

    fun TelegramItemRaceParticipantResponse.toModel(): TelegramItemRacePosition =
        TelegramItemRacePosition(
            id = id,
            raceId = raceId,
            accountNickname = accountNickname,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
