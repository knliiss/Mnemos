package com.mnemos.wallet.adapter.out.persistence.balance

import com.mnemos.wallet.adapter.out.persistence.common.WalletOwnerPersistenceMapper
import com.mnemos.wallet.domain.balance.model.Balance
import com.mnemos.wallet.domain.balance.model.BalanceAmount
import com.mnemos.wallet.domain.transaction.model.TransactionId

object BalancePersistenceMapper {
    fun BalanceEntity.toDomain(): Balance =
        Balance.restore(
            owner = WalletOwnerPersistenceMapper.toDomain(id.ownerType, id.ownerId),
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
                    ownerType = owner.type,
                    ownerId = owner.id,
                    currency = currency,
                ),
            amount = amount.value,
            lastTransactionId = lastTransactionId?.id,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
