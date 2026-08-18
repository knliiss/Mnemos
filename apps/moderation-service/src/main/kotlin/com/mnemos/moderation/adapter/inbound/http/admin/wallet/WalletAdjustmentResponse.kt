package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentStatus
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.time.Instant
import java.util.UUID

data class WalletAdjustmentResponse(
    val operationId: UUID,
    val requestId: UUID,
    val targetType: WalletOwnerType,
    val targetId: UUID,
    val actorUserId: UUID,
    val currency: Currency,
    val type: TransactionType,
    val amount: Long,
    val reason: String,
    val status: WalletAdjustmentStatus,
    val walletTransactionId: UUID?,
    val failureReason: String?,
    val createdAt: Instant,
)
