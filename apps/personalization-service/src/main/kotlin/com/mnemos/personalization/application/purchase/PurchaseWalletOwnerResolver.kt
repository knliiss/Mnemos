package com.mnemos.personalization.application.purchase

import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.purchase.model.PurchaseOperation
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType

object PurchaseWalletOwnerResolver {
    fun resolve(operation: PurchaseOperation): WalletOwner =
        when (operation.currency) {
            Currency.COIN -> {
                val accountOwner =
                    operation.owner as? PersonalizationOwner.Account
                        ?: error("COIN personalization purchase requires an account owner")

                WalletOwner(
                    type = WalletOwnerType.ACCOUNT,
                    id = accountOwner.accountId.id,
                )
            }

            Currency.PRIME ->
                WalletOwner(
                    type = WalletOwnerType.USER,
                    id = operation.buyerUserId.id,
                )
        }
}
