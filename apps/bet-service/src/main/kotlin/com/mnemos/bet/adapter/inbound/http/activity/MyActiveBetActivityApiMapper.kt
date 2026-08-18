package com.mnemos.bet.adapter.inbound.http.activity

import com.mnemos.bet.adapter.inbound.http.eventbet.EventBetApiMapper.toResponse
import com.mnemos.bet.adapter.inbound.http.itemrace.ItemRaceApiMapper.toResponse
import com.mnemos.bet.application.activity.model.MyActiveBetActivity
import com.mnemos.bet.domain.wallet.model.WalletOperation

object MyActiveBetActivityApiMapper {
    fun MyActiveBetActivity.toResponse(): MyActiveBetActivityResponse =
        MyActiveBetActivityResponse(
            eventBets =
                eventBets.map { activity ->
                    ActiveEventBetResponse(
                        round = activity.round.toResponse(),
                        bet = activity.bet.toResponse(),
                    )
                },
            itemRaces =
                itemRaces.map { activity ->
                    ActiveItemRaceResponse(
                        race = activity.race.toResponse(),
                        participant = activity.participant.toResponse(),
                    )
                },
            pendingOperations = pendingOperations.map { operation -> operation.toResponse() },
        )

    private fun WalletOperation.toResponse(): PendingBetOperationResponse =
        PendingBetOperationResponse(
            operationId = id.id,
            kind = kind,
            amount = amount,
            eventBetId = eventBetId?.id,
            itemRaceParticipantId = raceParticipantId?.id,
            createdAt = createdAt,
        )
}
