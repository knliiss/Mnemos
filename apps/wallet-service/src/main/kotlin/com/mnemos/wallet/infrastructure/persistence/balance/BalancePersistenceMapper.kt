package com.mnemos.wallet.infrastructure.persistence.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.domain.model.balance.Balance
import com.mnemos.wallet.domain.model.balance.BalanceAmount
import com.mnemos.wallet.domain.model.transaction.TransactionId

object BalancePersistenceMapper {
    fun BalanceEntity.toDomain(): Balance =
        Balance.restore(
            accountId = AccountId(id.accountId),
            currency = id.currency,
            amount = BalanceAmount(amount),
            lastTransactionId = lastTransactionId?.let(::TransactionId),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun Balance.toEntity(): BalanceEntity =
        BalanceEntity(
            id =
                BalanceEntityId(
                    accountId = accountId.id,
                    currency = currency,
                ),
            amount = amount.value,
            lastTransactionId = lastTransactionId?.id,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
