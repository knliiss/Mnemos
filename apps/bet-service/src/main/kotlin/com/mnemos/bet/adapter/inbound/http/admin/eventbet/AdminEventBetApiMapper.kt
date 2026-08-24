package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import com.mnemos.bet.application.eventbet.model.AdminEventBetView
import com.mnemos.bet.contracts.shared.BetSide

object AdminEventBetApiMapper {
    fun AdminEventBetView.toResponse(): AdminEventBetResponse =
        AdminEventBetResponse(
            id = id.id,
            roundId = roundId.id,
            accountId = accountId.id,
            accountNickname = accountNickname,
            side = BetSide.valueOf(side.name),
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
