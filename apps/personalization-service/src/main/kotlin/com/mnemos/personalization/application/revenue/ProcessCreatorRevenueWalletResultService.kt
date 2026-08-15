package com.mnemos.personalization.application.revenue

import com.mnemos.personalization.application.wallet.model.WalletResultContext
import com.mnemos.personalization.configuration.revenue.CreatorRevenueProperties
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperation
import com.mnemos.personalization.domain.revenue.model.CreatorRevenueOperationStatus
import com.mnemos.personalization.domain.revenue.repository.CreatorRevenueOperationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class ProcessCreatorRevenueWalletResultService(
    private val repository: CreatorRevenueOperationRepository,
    private val properties: CreatorRevenueProperties,
    private val clock: Clock,
) {
    @Transactional
    fun complete(
        result: WalletResultContext,
        transactionId: UUID,
    ) {
        val operation = repository.findByRequestIdForUpdate(result.requestId) ?: return

        if (operation.status != CreatorRevenueOperationStatus.PENDING) {
            return
        }

        requireExpectedResult(operation, result)

        operation.complete(transactionId)
        repository.save(operation)
    }

    @Transactional
    fun fail(
        result: WalletResultContext,
        reason: String,
    ) {
        val operation = repository.findByRequestIdForUpdate(result.requestId) ?: return

        if (operation.status != CreatorRevenueOperationStatus.PENDING) {
            return
        }

        requireExpectedResult(operation, result)

        val now = clock.instant()

        operation.fail(
            reason = reason,
            retryAt = now.plus(properties.revenueRetryDelay),
        )
        repository.save(operation)
    }

    private fun requireExpectedResult(
        operation: CreatorRevenueOperation,
        result: WalletResultContext,
    ) {
        check(result.actorUserId == null) {
            "Creator revenue wallet result must not have an actor user"
        }
        check(result.ownerType == WalletOwnerType.USER && result.ownerId == operation.creatorUserId.id) {
            "Creator revenue wallet result owner does not match operation"
        }
        check(result.currency == Currency.PRIME) {
            "Creator revenue wallet result must use PRIME"
        }
        check(result.transactionType == TransactionType.CREDIT) {
            "Creator revenue wallet result must be a credit"
        }
        check(result.source == TransactionSource.CREATOR_REVENUE) {
            "Creator revenue wallet result source does not match operation"
        }
        check(result.sourceReferenceId == operation.id.id) {
            "Creator revenue wallet result source reference does not match operation"
        }
        check(result.amount == operation.amountPrime) {
            "Creator revenue wallet result amount does not match operation"
        }
    }
}
