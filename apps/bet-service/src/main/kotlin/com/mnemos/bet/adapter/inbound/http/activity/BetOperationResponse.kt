package com.mnemos.bet.adapter.inbound.http.activity

import com.mnemos.bet.domain.wallet.model.WalletOperationKind
import com.mnemos.bet.domain.wallet.model.WalletOperationStatus
import java.time.Instant
import java.util.UUID

data class BetOperationResponse(
    val operationId: UUID,
    val accountId: UUID,
    val amount: Long,
    val kind: WalletOperationKind,
    val status: WalletOperationStatus,
    val eventBetId: UUID?,
    val itemRaceParticipantId: UUID?,
    val walletTransactionId: UUID?,
    val createdAt: Instant,
)
