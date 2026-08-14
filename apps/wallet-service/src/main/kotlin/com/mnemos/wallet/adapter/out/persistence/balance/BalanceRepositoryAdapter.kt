package com.mnemos.wallet.adapter.out.persistence.balance

import com.mnemos.wallet.adapter.out.persistence.balance.BalancePersistenceMapper.toDomain
import com.mnemos.wallet.adapter.out.persistence.balance.BalancePersistenceMapper.toEntity
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.balance.repository.BalanceRepository
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.stereotype.Repository

@Repository
class BalanceRepositoryAdapter(
    private val balanceJpaRepository: BalanceJpaRepository,
    private val balancePersistenceWriter: BalancePersistenceWriter,
) : BalanceRepository {
    override fun findForUpdate(
        owner: WalletOwner,
        currency: Currency,
    ): Balance? =
        balanceJpaRepository
            .findForUpdate(
                ownerType = owner.type,
                ownerId = owner.id,
                currency = currency,
            )?.toDomain()

    override fun createIfAbsent(balance: Balance): Boolean = balancePersistenceWriter.insertIfAbsent(balance.toEntity())

    override fun update(balance: Balance) {
        val updatedRows =
            balanceJpaRepository.updateBalance(
                ownerType = balance.owner.type,
                ownerId = balance.owner.id,
                currency = balance.currency,
                amount = balance.amount.value,
                lastTransactionId = balance.lastTransactionId?.id,
                updatedAt = balance.updatedAt,
            )

        check(updatedRows == 1) { "Expected one balance row to be updated, but updated $updatedRows." }
    }
}
