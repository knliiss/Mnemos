package com.mnemos.bet.adapter.inbound.http.admin.wallet

import com.mnemos.bet.application.wallet.usecase.failed.FailedWalletOperationView

object AdminWalletOperationMapper {
    fun FailedWalletOperationView.toResponse(): FailedWalletOperationResponse =
        FailedWalletOperationResponse(
            id = id.id,
            accountId = accountId.id,
            amount = amount,
            kind = kind,
            eventBetId = eventBetId?.id,
            eventBetResolutionId = eventBetResolutionId?.id,
            raceParticipantId = raceParticipantId?.id,
            createdAt = createdAt,
        )
}
