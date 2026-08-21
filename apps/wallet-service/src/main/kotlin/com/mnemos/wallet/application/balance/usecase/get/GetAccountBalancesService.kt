package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.application.balance.port.out.BalanceReader
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetAccountBalancesService(
    private val balanceReader: BalanceReader,
) : GetAccountBalancesUseCase {
    override fun execute(accountId: AccountId): List<BalanceView> {
        val balances =
            balanceReader
                .findByOwner(WalletOwner.Account(accountId))
                .associateBy(BalanceView::currency)

        return WalletCurrencyPolicy
            .supportedCurrencies(WalletOwnerType.ACCOUNT)
            .map { currency -> balances[currency] ?: BalanceView(currency = currency, amount = 0) }
    }
}
