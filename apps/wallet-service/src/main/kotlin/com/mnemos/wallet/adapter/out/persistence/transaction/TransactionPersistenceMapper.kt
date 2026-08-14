package com.mnemos.wallet.adapter.out.persistence.transaction

import com.mnemos.wallet.adapter.out.persistence.common.WalletOwnerPersistenceMapper
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.Transaction
import com.mnemos.wallet.domain.transaction.model.TransactionAmount
import com.mnemos.wallet.domain.transaction.model.TransactionId
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId

object TransactionPersistenceMapper {
    fun TransactionEntity.toDomain(): Transaction =
        Transaction.restore(
            id = TransactionId(id),
            owner = WalletOwnerPersistenceMapper.toDomain(ownerType, ownerId),
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = TransactionSourceReferenceId(sourceReferenceId),
            amount = TransactionAmount(amount),
            requestId = RequestId(requestId),
            createdAt = createdAt,
        )

    fun Transaction.toEntity(): TransactionEntity =
        TransactionEntity(
            id = id.id,
            ownerType = owner.type,
            ownerId = owner.id,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId.id,
            amount = amount.value,
            requestId = requestId.id,
            createdAt = createdAt,
        )
}
