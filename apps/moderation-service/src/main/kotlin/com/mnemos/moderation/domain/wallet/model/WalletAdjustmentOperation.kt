package com.mnemos.moderation.domain.wallet.model

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.UserId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.time.Instant
import java.util.UUID

class WalletAdjustmentOperation(
    val id: WalletAdjustmentOperationId,
    val requestId: UUID,
    val targetType: WalletOwnerType,
    val targetId: UUID,
    val actorUserId: UserId,
    val currency: Currency,
    val type: TransactionType,
    val amount: Long,
    val reason: String,
    val createdAt: Instant,
    status: WalletAdjustmentStatus,
    walletTransactionId: UUID?,
    failureReason: String?,
    val idempotencyKey: UUID? = null,
) {
    var status: WalletAdjustmentStatus = status
        private set

    var walletTransactionId: UUID? = walletTransactionId
        private set

    var failureReason: String? = failureReason
        private set

    init {
        require(amount > 0) { "Wallet adjustment amount must be positive" }
        require(reason.isNotBlank()) { "Wallet adjustment reason cannot be blank" }
        require(reason.length <= MAX_REASON_LENGTH) { "Wallet adjustment reason is too long" }
        requireTargetSupportsCurrency(targetType, currency)
        requireValidResultState()
    }

    fun complete(transactionId: UUID) {
        if (status != WalletAdjustmentStatus.PENDING) {
            return
        }

        status = WalletAdjustmentStatus.COMPLETED
        walletTransactionId = transactionId
        failureReason = null
    }

    fun fail(reason: String) {
        if (status != WalletAdjustmentStatus.PENDING) {
            return
        }

        status = WalletAdjustmentStatus.FAILED
        walletTransactionId = null
        failureReason = reason.take(MAX_FAILURE_REASON_LENGTH)
    }

    private fun requireValidResultState() {
        when (status) {
            WalletAdjustmentStatus.PENDING ->
                require(walletTransactionId == null && failureReason == null)

            WalletAdjustmentStatus.COMPLETED ->
                require(walletTransactionId != null && failureReason == null)

            WalletAdjustmentStatus.FAILED ->
                require(walletTransactionId == null && !failureReason.isNullOrBlank())
        }
    }

    companion object {
        const val MAX_REASON_LENGTH = 256
        private const val MAX_FAILURE_REASON_LENGTH = 128

        fun create(
            targetType: WalletOwnerType,
            targetId: UUID,
            actorUserId: UserId,
            currency: Currency,
            type: TransactionType,
            amount: Long,
            reason: String,
            now: Instant,
            idempotencyKey: UUID? = null,
        ): WalletAdjustmentOperation =
            WalletAdjustmentOperation(
                id = WalletAdjustmentOperationId.generate(),
                requestId = UuidCreator.getTimeOrderedEpoch(),
                targetType = targetType,
                targetId = targetId,
                actorUserId = actorUserId,
                currency = currency,
                type = type,
                amount = amount,
                reason = reason.trim(),
                createdAt = now,
                status = WalletAdjustmentStatus.PENDING,
                walletTransactionId = null,
                failureReason = null,
                idempotencyKey = idempotencyKey,
            )

        private fun requireTargetSupportsCurrency(
            targetType: WalletOwnerType,
            currency: Currency,
        ) {
            val supported =
                when (currency) {
                    Currency.COIN -> targetType == WalletOwnerType.ACCOUNT
                    Currency.PRIME -> targetType == WalletOwnerType.USER
                }

            require(supported) {
                "Currency $currency cannot belong to wallet owner type $targetType"
            }
        }
    }
}
