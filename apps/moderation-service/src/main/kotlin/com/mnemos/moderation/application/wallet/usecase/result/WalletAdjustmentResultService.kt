package com.mnemos.moderation.application.wallet.usecase.result

import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation
import com.mnemos.moderation.domain.wallet.repository.WalletAdjustmentOperationRepository
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionCompletedEvent
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WalletAdjustmentResultService(
    private val repository: WalletAdjustmentOperationRepository,
) {
    @Transactional
    fun complete(event: WalletTransactionCompletedEvent) {
        if (event.source != TransactionSource.ADMIN_ADJUSTMENT) {
            return
        }

        val operation = repository.findByRequestIdForUpdate(event.requestId) ?: return

        requireMatches(
            operation = operation,
            sourceReferenceId = event.sourceReferenceId,
            ownerType = event.owner.type,
            ownerId = event.owner.id,
            currency = event.currency,
            type = event.type,
            amount = event.amount,
        )

        operation.complete(event.transactionId)
        repository.save(operation)
    }

    @Transactional
    fun fail(event: WalletTransactionFailedEvent) {
        if (event.source != TransactionSource.ADMIN_ADJUSTMENT) {
            return
        }

        val operation = repository.findByRequestIdForUpdate(event.requestId) ?: return

        requireMatches(
            operation = operation,
            sourceReferenceId = event.sourceReferenceId,
            ownerType = event.owner.type,
            ownerId = event.owner.id,
            currency = event.currency,
            type = event.type,
            amount = event.amount,
        )

        operation.fail(event.reason.name)
        repository.save(operation)
    }

    private fun requireMatches(
        operation: WalletAdjustmentOperation,
        sourceReferenceId: UUID,
        ownerType: WalletOwnerType,
        ownerId: UUID,
        currency: Currency,
        type: TransactionType,
        amount: Long,
    ) {
        require(sourceReferenceId == operation.id.id) { "Wallet adjustment source reference mismatch" }
        require(ownerType == operation.targetType) { "Wallet adjustment owner type mismatch" }
        require(ownerId == operation.targetId) { "Wallet adjustment owner mismatch" }
        require(currency == operation.currency) { "Wallet adjustment currency mismatch" }
        require(type == operation.type) { "Wallet adjustment transaction type mismatch" }
        require(amount == operation.amount) { "Wallet adjustment amount mismatch" }
    }
}
