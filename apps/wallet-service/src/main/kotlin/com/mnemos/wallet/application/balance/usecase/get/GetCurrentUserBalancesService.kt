package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.application.balance.port.out.BalanceReader
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetCurrentUserBalancesService(
    private val balanceReader: BalanceReader,
    private val currentUserProvider: CurrentUserProvider,
) : GetCurrentUserBalancesUseCase {
    override fun execute(): List<BalanceView> {
        val userId = currentUserProvider.requireUserId()
        val balances =
            balanceReader
                .findByOwner(WalletOwner.User(userId))
                .associateBy(BalanceView::currency)

        return WalletCurrencyPolicy
            .supportedCurrencies(WalletOwnerType.USER)
            .map { currency -> balances[currency] ?: BalanceView(currency = currency, amount = 0) }
    }
}
