package com.mnemos.moderation.application.wallet.usecase.query

import com.mnemos.moderation.application.wallet.exception.WalletAdjustmentOperationNotFoundException
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId
import com.mnemos.moderation.domain.wallet.repository.WalletAdjustmentOperationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WalletAdjustmentQueryService(
    private val repository: WalletAdjustmentOperationRepository,
) {
    fun get(id: WalletAdjustmentOperationId): WalletAdjustmentOperation =
        repository.findById(id)
            ?: throw WalletAdjustmentOperationNotFoundException(id)

    fun list(query: ListWalletAdjustmentsQuery): List<WalletAdjustmentOperation> =
        repository.findByTarget(
            targetType = query.targetType,
            targetId = query.targetId,
        )
}
