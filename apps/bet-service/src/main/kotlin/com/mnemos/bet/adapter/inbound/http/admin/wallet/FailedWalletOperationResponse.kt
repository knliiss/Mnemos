package com.mnemos.bet.adapter.inbound.http.admin.wallet

import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import java.time.Instant
import java.util.UUID

data class FailedWalletOperationResponse(
    val id: UUID,
    val accountId: UUID,
    val amount: Long,
    val kind: WalletOperationKind,
    val eventBetId: UUID?,
    val eventBetResolutionId: UUID?,
    val raceParticipantId: UUID?,
    val createdAt: Instant,
)
