package com.mnemos.bet.adapter.inbound.http.internal

import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceParticipantResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceResponse

object TelegramItemRaceInternalApiMapper {
    fun GetItemRaceResult.toTelegramResponse(): TelegramItemRaceResponse =
        TelegramItemRaceResponse(
            id = race.id.id,
            itemName = race.itemName,
            itemType = race.itemType.name,
            registrationClosesAt = race.registrationClosesAt,
            startsAt = race.startsAt,
            expiresAt = race.expiresAt,
            entryAmount = race.entryAmount,
            feePercent = race.feePercent,
            status = race.status.name,
            winnerAccountId = race.winnerAccountId?.id,
            myPosition = participants.singleOrNull()?.toTelegramResponse(),
        )

    fun ItemRaceParticipantView.toTelegramResponse(): TelegramItemRaceParticipantResponse =
        TelegramItemRaceParticipantResponse(
            id = id.id,
            raceId = raceId.id,
            accountNickname = accountNickname,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status.name,
        )
}
