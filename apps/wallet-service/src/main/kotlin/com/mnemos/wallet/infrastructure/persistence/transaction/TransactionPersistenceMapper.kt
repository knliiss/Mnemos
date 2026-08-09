package com.mnemos.wallet.infrastructure.persistence.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.Transaction
import com.mnemos.wallet.domain.model.transaction.TransactionAmount
import com.mnemos.wallet.domain.model.transaction.TransactionId
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId

object TransactionPersistenceMapper {
    fun TransactionEntity.toDomain(): Transaction =
        Transaction.restore(
            id = TransactionId(id),
            accountId = AccountId(accountId),
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
            accountId = accountId.id,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId.id,
            amount = amount.value,
            requestId = requestId.id,
            createdAt = createdAt,
        )
}
