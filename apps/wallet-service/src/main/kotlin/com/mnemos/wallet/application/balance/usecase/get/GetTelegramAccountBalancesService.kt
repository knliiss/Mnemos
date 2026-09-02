package com.mnemos.wallet.application.balance.usecase.get

import com.mnemos.wallet.application.account.port.out.TelegramActiveAccountProvider
import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetTelegramAccountBalancesService(
    private val telegramActiveAccountProvider: TelegramActiveAccountProvider,
    private val getWalletBalancesService: GetWalletBalancesService,
) {
    fun execute(telegramId: Long): List<BalanceView>? {
        val activeAccountId = telegramActiveAccountProvider.findActiveAccountId(telegramId) ?: return null

        return getWalletBalancesService.execute(
            ownerType = WalletOwnerType.ACCOUNT,
            ownerId = activeAccountId.id,
        )
    }
}
