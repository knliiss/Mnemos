package com.mnemos.personalization.domain.purchase.model

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import java.time.Instant
import java.util.UUID

class PurchaseOperation(
    val id: PurchaseOperationId,
    val requestId: UUID,
    val personalizationId: PersonalizationId,
    val buyerUserId: UserId,
    val owner: PersonalizationOwner,
    val currency: Currency,
    val amount: Long,
    val createdAt: Instant,
    status: PurchaseOperationStatus,
    walletTransactionId: UUID?,
    failureReason: String?,
) {
    var status: PurchaseOperationStatus = status
        private set
    var walletTransactionId: UUID? = walletTransactionId
        private set
    var failureReason: String? = failureReason
        private set

    init {
        require(amount > 0) { "Purchase amount must be positive" }
        requireValidResultState()
    }

    fun complete(transactionId: UUID) {
        require(status == PurchaseOperationStatus.PENDING) { "Only pending purchase can be completed" }

        status = PurchaseOperationStatus.CONFIRMED
        walletTransactionId = transactionId
        failureReason = null
    }

    fun fail(reason: String) {
        require(status == PurchaseOperationStatus.PENDING) { "Only pending purchase can fail" }
        require(reason.isNotBlank()) { "Purchase failure reason cannot be blank" }
        require(reason.length <= MAX_FAILURE_REASON_LENGTH) { "Purchase failure reason is too long" }

        status = PurchaseOperationStatus.FAILED
        walletTransactionId = null
        failureReason = reason
    }

    private fun requireValidResultState() {
        failureReason?.let { reason ->
            require(reason.isNotBlank()) { "Purchase failure reason cannot be blank" }
            require(reason.length <= MAX_FAILURE_REASON_LENGTH) { "Purchase failure reason is too long" }
        }

        when (status) {
            PurchaseOperationStatus.PENDING ->
                require(walletTransactionId == null && failureReason == null) {
                    "Pending purchase cannot contain a wallet result"
                }

            PurchaseOperationStatus.CONFIRMED ->
                require(walletTransactionId != null && failureReason == null) {
                    "Confirmed purchase requires a wallet transaction and no failure reason"
                }

            PurchaseOperationStatus.FAILED ->
                require(walletTransactionId == null && failureReason != null) {
                    "Failed purchase requires a failure reason and no wallet transaction"
                }
        }
    }

    companion object {
        private const val MAX_FAILURE_REASON_LENGTH = 128

        fun create(
            personalizationId: PersonalizationId,
            buyerUserId: UserId,
            owner: PersonalizationOwner,
            currency: Currency,
            amount: Long,
            now: Instant,
        ): PurchaseOperation =
            PurchaseOperation(
                id = PurchaseOperationId.generate(),
                requestId = UuidCreator.getTimeOrderedEpoch(),
                personalizationId = personalizationId,
                buyerUserId = buyerUserId,
                owner = owner,
                currency = currency,
                amount = amount,
                createdAt = now,
                status = PurchaseOperationStatus.PENDING,
                walletTransactionId = null,
                failureReason = null,
            )
    }
}
