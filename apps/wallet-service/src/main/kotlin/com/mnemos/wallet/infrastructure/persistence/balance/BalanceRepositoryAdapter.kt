package com.mnemos.wallet.infrastructure.persistence.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.api.dto.BalanceResponse
import com.mnemos.wallet.api.dto.toBalanceResponse
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.model.balance.Balance
import com.mnemos.wallet.domain.repository.BalanceRepository
import com.mnemos.wallet.infrastructure.persistence.balance.BalancePersistenceMapper.toDomain
import com.mnemos.wallet.infrastructure.persistence.balance.BalancePersistenceMapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class BalanceRepositoryAdapter(
    private val balanceJpaRepository: BalanceJpaRepository,
    private val balancePersistenceWriter: BalancePersistenceWriter,
) : BalanceRepository {
    override fun findByAccountIdAndCurrencyForUpdate(
        accountId: AccountId,
        currency: Currency,
    ): Balance? =
        balanceJpaRepository
            .findByAccountIdAndCurrencyForUpdate(
                accountId = accountId.id,
                currency = currency,
            )?.toDomain()

    override fun update(balance: Balance) {
        val updatedRows =
            balanceJpaRepository.updateBalance(
                accountId = balance.accountId.id,
                currency = balance.currency,
                amount = balance.amount.value,
                lastTransactionId = balance.lastTransactionId?.id,
                updatedAt = balance.updatedAt,
            )

        check(updatedRows == 1) {
            "Expected one balance row to be updated, but updated $updatedRows."
        }
    }

    override fun createIfAbsent(balance: Balance): Boolean = balancePersistenceWriter.insertIfAbsent(balance.toEntity())

    override fun findAllByAccountId(accountId: AccountId): List<BalanceResponse> =
        balanceJpaRepository.findAllByAccountId(accountId.id).map {
            it.toDomain().toBalanceResponse()
        }
}
