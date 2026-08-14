package com.mnemos.wallet.adapter.out.persistence.balance

import com.mnemos.wallet.application.balance.model.BalanceView
import com.mnemos.wallet.application.balance.port.out.BalanceReader
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.springframework.stereotype.Repository

@Repository
class BalanceReaderAdapter(
    private val balanceJpaRepository: BalanceJpaRepository,
) : BalanceReader {
    override fun findByOwner(owner: WalletOwner): List<BalanceView> {
        val balances =
            balanceJpaRepository
                .findAllByOwner(
                    ownerType = owner.type,
                    ownerId = owner.id,
                ).associateBy { it.id.currency }

        return WalletCurrencyPolicy
            .supportedCurrencies(owner.type)
            .map { currency ->
                BalanceView(
                    currency = currency,
                    amount = balances[currency]?.amount ?: 0,
                )
            }
    }
}
