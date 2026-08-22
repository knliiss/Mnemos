package com.mnemos.moderation.adapter.out.wallet

import com.mnemos.moderation.application.wallet.model.WalletBalance
import com.mnemos.moderation.application.wallet.port.out.ModerationWalletBalanceReader
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class WalletBalanceReaderAdapter(
    private val walletServiceClient: WalletServiceClient,
) : ModerationWalletBalanceReader {
    override fun getBalance(
        ownerType: WalletOwnerType,
        ownerId: UUID,
    ): WalletBalance {
        val currency =
            when (ownerType) {
                WalletOwnerType.ACCOUNT -> Currency.COIN
                WalletOwnerType.USER -> Currency.PRIME
            }
        val balances = walletServiceClient.getBalances(ownerType, ownerId)
        val amount = balances.firstOrNull { balance -> balance.currency == currency }?.amount ?: 0

        return WalletBalance(
            ownerType = ownerType,
            ownerId = ownerId,
            currency = currency,
            amount = amount,
        )
    }
}
