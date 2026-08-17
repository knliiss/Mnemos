package com.mnemos.moderation.adapter.out.persistence.wallet

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId

object WalletAdjustmentOperationPersistenceMapper {
    fun WalletAdjustmentOperation.toEntity(): WalletAdjustmentOperationEntity =
        WalletAdjustmentOperationEntity(
            id = id.id,
            requestId = requestId,
            idempotencyKey = idempotencyKey,
            targetType = targetType,
            targetId = targetId,
            actorUserId = actorUserId.id,
            currency = currency,
            type = type,
            amount = amount,
            reason = reason,
            createdAt = createdAt,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
        )

    fun WalletAdjustmentOperationEntity.toDomain(): WalletAdjustmentOperation =
        WalletAdjustmentOperation(
            id = WalletAdjustmentOperationId(id),
            requestId = requestId,
            targetType = targetType,
            targetId = targetId,
            actorUserId = UserId(actorUserId),
            currency = currency,
            type = type,
            amount = amount,
            reason = reason,
            createdAt = createdAt,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            idempotencyKey = idempotencyKey,
        )
}
