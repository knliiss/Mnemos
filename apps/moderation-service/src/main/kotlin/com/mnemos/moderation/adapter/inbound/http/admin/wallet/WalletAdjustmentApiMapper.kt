package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.moderation.application.wallet.usecase.create.CreateWalletAdjustmentCommand
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import java.util.UUID

object WalletAdjustmentApiMapper {
    fun CreateWalletAdjustmentRequest.toCommand(idempotencyKey: UUID): CreateWalletAdjustmentCommand =
        CreateWalletAdjustmentCommand(
            idempotencyKey = idempotencyKey,
            targetType = targetType,
            targetId = targetId,
            currency = currency,
            type = type,
            amount = amount,
            reason = reason,
        )

    fun WalletAdjustmentOperation.toResponse(): WalletAdjustmentResponse =
        WalletAdjustmentResponse(
            operationId = id.id,
            requestId = requestId,
            targetType = targetType,
            targetId = targetId,
            actorUserId = actorUserId.id,
            currency = currency,
            type = type,
            amount = amount,
            reason = reason,
            status = status,
            walletTransactionId = walletTransactionId,
            failureReason = failureReason,
            createdAt = createdAt,
        )
}
