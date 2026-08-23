package com.mnemos.wallet.application.balance.usecase.initialize

import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.balance.repository.BalanceRepository
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import com.mnemos.wallet.domain.wallet.policy.WalletCurrencyPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InitializeWalletBalancesService(
    private val balanceRepository: BalanceRepository,
) {
    @Transactional
    fun execute(
        owner: WalletOwner,
        occurredAt: Instant,
    ) {
        WalletCurrencyPolicy
            .supportedCurrencies(owner.type)
            .forEach { currency ->
                balanceRepository.createIfAbsent(
                    Balance.create(
                        owner = owner,
                        currency = currency,
                        createdAt = occurredAt,
                    ),
                )
            }
    }
}
