package com.mnemos.moderation.application.wallet.usecase.query

import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId
import com.mnemos.moderation.domain.wallet.repository.WalletAdjustmentOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
@Transactional(readOnly = true)
class WalletAdjustmentQueryService(
    private val repository: WalletAdjustmentOperationRepository,
) {
    fun get(id: WalletAdjustmentOperationId): WalletAdjustmentOperation =
        repository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet adjustment operation was not found")

    fun list(
        targetType: WalletOwnerType,
        targetId: UUID,
    ): List<WalletAdjustmentOperation> = repository.findByTarget(targetType, targetId)
}
