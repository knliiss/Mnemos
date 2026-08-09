package com.mnemos.wallet.application.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.model.balance.Balance
import com.mnemos.wallet.domain.repository.BalanceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class InitializeAccountBalancesService(
    private val balanceRepository: BalanceRepository,
) {
    @Transactional
    fun initialize(accountId: AccountId) {
        val createdAt = Instant.now()

        Currency.entries.forEach { currency ->
            balanceRepository.createIfAbsent(
                Balance.create(
                    accountId = accountId,
                    currency = currency,
                    createdAt = createdAt,
                ),
            )
        }
    }
}
