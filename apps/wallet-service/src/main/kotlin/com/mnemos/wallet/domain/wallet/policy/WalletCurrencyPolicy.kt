package com.mnemos.wallet.domain.wallet.policy

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.wallet.model.WalletOwner

object WalletCurrencyPolicy {
    fun supports(
        owner: WalletOwner,
        currency: Currency,
    ): Boolean = supports(owner.type, currency)

    fun supports(
        ownerType: WalletOwnerType,
        currency: Currency,
    ): Boolean =
        when (currency) {
            Currency.COIN -> ownerType == WalletOwnerType.ACCOUNT
            Currency.PRIME -> ownerType == WalletOwnerType.USER
        }

    fun supportedCurrencies(ownerType: WalletOwnerType): List<Currency> =
        when (ownerType) {
            WalletOwnerType.ACCOUNT -> listOf(Currency.COIN)
            WalletOwnerType.USER -> listOf(Currency.PRIME)
        }

    fun requireSupported(
        owner: WalletOwner,
        currency: Currency,
    ) {
        require(supports(owner, currency)) {
            "Currency $currency cannot belong to wallet owner type ${owner.type}."
        }
    }
}
