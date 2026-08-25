package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import com.mnemos.bet.application.eventbet.model.AdminEventBetRoundBettorView
import com.mnemos.bet.application.eventbet.model.AdminEventBetRoundMetadataView

object AdminEventBetRoundMetadataApiMapper {
    fun AdminEventBetRoundMetadataView.toResponse(): AdminEventBetRoundMetadataResponse =
        AdminEventBetRoundMetadataResponse(
            roundId = roundId.id,
            betCount = betCount,
            bettors = bettors.map { bettor -> bettor.toResponse() },
        )

    private fun AdminEventBetRoundBettorView.toResponse(): AdminEventBetRoundBettorResponse =
        AdminEventBetRoundBettorResponse(
            accountId = accountId.id,
            accountNickname = accountNickname,
        )
}
