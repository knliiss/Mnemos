package com.mnemos.bet.adapter.inbound.http.activity

import com.mnemos.bet.domain.wallet.model.WalletOperation

object BetOperationApiMapper {
    fun WalletOperation.toResponse(): BetOperationResponse =
        BetOperationResponse(
            operationId = id.id,
            accountId = accountId.id,
            amount = amount,
            kind = kind,
            status = status,
            eventBetId = eventBetId?.id,
            itemRaceParticipantId = raceParticipantId?.id,
            walletTransactionId = walletTransactionId?.id,
            createdAt = createdAt,
        )
}
