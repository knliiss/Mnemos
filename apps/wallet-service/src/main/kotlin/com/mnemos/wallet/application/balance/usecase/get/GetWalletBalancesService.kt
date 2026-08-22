package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.application.balance.port.out.BalanceReader
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetWalletBalancesService(
    private val balanceReader: BalanceReader,
) {
    fun execute(
        ownerType: WalletOwnerType,
        ownerId: UUID,
    ): List<BalanceView> {
        val owner =
            when (ownerType) {
                WalletOwnerType.ACCOUNT -> WalletOwner.Account(AccountId(ownerId))
                WalletOwnerType.USER -> WalletOwner.User(UserId(ownerId))
            }
        val balances =
            balanceReader
                .findByOwner(owner)
                .associateBy(BalanceView::currency)

        return WalletCurrencyPolicy
            .supportedCurrencies(ownerType)
            .map { currency -> balances[currency] ?: BalanceView(currency = currency, amount = 0) }
    }
}
