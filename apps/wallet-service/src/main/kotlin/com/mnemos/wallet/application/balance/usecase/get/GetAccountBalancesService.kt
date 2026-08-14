package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.web.exception.common.AccountPermissionDeniedException
import com.mnemos.wallet.application.account.port.out.AccountPermissionProvider
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
class GetAccountBalancesService(
    private val balanceReader: BalanceReader,
    private val currentUserProvider: CurrentUserProvider,
    private val accountPermissionProvider: AccountPermissionProvider,
) : GetAccountBalancesUseCase {
    override fun execute(accountId: AccountId): List<BalanceView> {
        val userId = currentUserProvider.requireUserId()
        val granted =
            accountPermissionProvider.hasPermission(
                accountId = accountId,
                userId = userId,
                permission = RolePermission.VIEW_WALLET,
            )

        if (!granted) {
            throw AccountPermissionDeniedException(accountId)
        }

        val balances =
            balanceReader
                .findByOwner(WalletOwner.Account(accountId))
                .associateBy(BalanceView::currency)

        return WalletCurrencyPolicy
            .supportedCurrencies(WalletOwnerType.ACCOUNT)
            .map { currency -> balances[currency] ?: BalanceView(currency = currency, amount = 0) }
    }
}
