package com.mnemos.wallet.adapter.out.persistence.request

import com.mnemos.wallet.adapter.out.persistence.common.WalletOwnerPersistenceMapper
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.TransactionId
import com.mnemos.wallet.domain.transaction.model.TransactionRequest
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId

object TransactionRequestPersistenceMapper {
    fun TransactionRequestEntity.toDomain(): TransactionRequest =
        TransactionRequest.restore(
            requestId = RequestId(requestId),
            owner = WalletOwnerPersistenceMapper.toDomain(ownerType, ownerId),
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
            ownerType = owner.type,
            ownerId = owner.id,
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
