package com.mnemos.moderation.domain.wallet.repository

import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

interface WalletAdjustmentOperationRepository {
    fun save(operation: WalletAdjustmentOperation): WalletAdjustmentOperation

    fun findById(id: WalletAdjustmentOperationId): WalletAdjustmentOperation?

    fun findByIdempotencyKey(idempotencyKey: UUID): WalletAdjustmentOperation?

    fun findByRequestIdForUpdate(requestId: UUID): WalletAdjustmentOperation?

    fun findByTarget(
        targetType: WalletOwnerType,
        targetId: UUID,
    ): List<WalletAdjustmentOperation>
}
