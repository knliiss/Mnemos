package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateWalletAdjustmentRequest(
    val targetType: WalletOwnerType,
    val targetId: UUID,
    val currency: Currency,
    val type: TransactionType,
    @field:Positive
    val amount: Long,
    @field:NotBlank
    @field:Size(max = 256)
    val reason: String,
)
