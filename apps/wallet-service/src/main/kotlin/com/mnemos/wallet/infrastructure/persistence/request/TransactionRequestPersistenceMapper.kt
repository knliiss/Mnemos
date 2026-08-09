package com.mnemos.wallet.infrastructure.persistence.request

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.domain.model.request.TransactionRequest
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.TransactionId
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId

object TransactionRequestPersistenceMapper {
    fun TransactionRequestEntity.toDomain(): TransactionRequest =
        TransactionRequest.restore(
            requestId = RequestId(requestId),
            accountId = AccountId(accountId),
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = TransactionSourceReferenceId(sourceReferenceId),
            amount = amount,
            status = status,
            transactionId = transactionId?.let(::TransactionId),
            failureReason = failureReason,
            createdAt = createdAt,
            completedAt = completedAt,
        )

    fun TransactionRequest.toEntity(): TransactionRequestEntity =
        TransactionRequestEntity(
            requestId = requestId.id,
            accountId = accountId.id,
            currency = currency,
            type = type,
            source = source,
            sourceReferenceId = sourceReferenceId.id,
            amount = amount,
            status = status,
            transactionId = transactionId?.id,
            failureReason = failureReason,
            createdAt = createdAt,
            completedAt = completedAt,
        )
}
