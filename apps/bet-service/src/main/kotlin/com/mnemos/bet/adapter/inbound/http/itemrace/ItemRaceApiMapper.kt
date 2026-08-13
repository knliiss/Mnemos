package com.mnemos.bet.adapter.inbound.http.itemrace

import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult
import com.mnemos.bet.contracts.shared.BetItemType

object ItemRaceApiMapper {
    fun GetItemRaceResult.toResponse(): ItemRaceResponse = race.toResponse(participants)

    fun ItemRaceView.toResponse(participants: List<ItemRaceParticipantView> = emptyList()): ItemRaceResponse =
        ItemRaceResponse(
            id = id.id,
            itemName = itemName,
            itemType = BetItemType.valueOf(itemType.name),
            registrationClosesAt = registrationClosesAt,
            startsAt = startsAt,
            expiresAt = expiresAt,
            entryAmount = entryAmount,
            feePercent = feePercent,
            status = status,
            winnerAccountId = winnerAccountId?.id,
            participants = participants.map { it.toResponse() },
        )

    fun ItemRaceParticipantView.toResponse(): ItemRaceParticipantResponse =
        ItemRaceParticipantResponse(
            id = id.id,
            raceId = raceId.id,
            accountId = accountId.id,
            accountNickname = accountNickname,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
