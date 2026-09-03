package com.mnemos.realtime.application.delivery.model

import java.util.UUID

sealed interface RealtimeEventPayload

data class WalletTransactionCompletedPayload(
    val requestId: UUID,
    val transactionId: UUID,
    val ownerType: String,
    val ownerId: UUID,
    val currency: String,
    val source: String,
    val sourceReferenceId: UUID,
    val amount: Long,
) : RealtimeEventPayload

data class WalletTransactionFailedPayload(
    val requestId: UUID,
    val ownerType: String,
    val ownerId: UUID,
    val currency: String,
    val source: String,
    val sourceReferenceId: UUID,
    val amount: Long,
    val reason: String,
) : RealtimeEventPayload

data class BetOperationCompletedPayload(
    val resourceType: String,
    val resourceId: UUID,
    val status: String,
    val amount: Long,
    val walletTransactionId: UUID?,
    val failureReason: String?,
) : RealtimeEventPayload

data class PersonalizationPurchaseCompletedPayload(
    val personalizationId: UUID,
    val status: String,
    val walletTransactionId: UUID?,
    val failureReason: String?,
) : RealtimeEventPayload {
    init {
        when (status) {
            CONFIRMED ->
                require(walletTransactionId != null && failureReason == null) {
                    "Confirmed personalization purchase requires a wallet transaction"
                }

            FAILED ->
                require(walletTransactionId == null && !failureReason.isNullOrBlank()) {
                    "Failed personalization purchase requires a failure reason and no wallet transaction"
                }

            else -> throw IllegalArgumentException("Unknown personalization purchase status: $status")
        }
    }

    private companion object {
        const val CONFIRMED = "CONFIRMED"
        const val FAILED = "FAILED"
    }
}

data class AccountStatusChangedPayload(
    val accountId: UUID,
    val status: String,
) : RealtimeEventPayload

data class AccountOwnershipTransferredPayload(
    val accountId: UUID,
    val previousOwnerMemberId: UUID,
    val previousOwnerUserId: UUID,
    val newOwnerMemberId: UUID,
    val newOwnerUserId: UUID,
) : RealtimeEventPayload

data class AccountMemberChangedPayload(
    val accountId: UUID,
    val memberId: UUID,
    val userId: UUID,
    val change: String,
) : RealtimeEventPayload

fun RealtimeEventPayload.eventType(): RealtimeEventType =
    when (this) {
        is WalletTransactionCompletedPayload ->
            RealtimeEventType.WALLET_TRANSACTION_COMPLETED

        is WalletTransactionFailedPayload ->
            RealtimeEventType.WALLET_TRANSACTION_FAILED

        is BetOperationCompletedPayload ->
            RealtimeEventType.BET_OPERATION_COMPLETED

        is PersonalizationPurchaseCompletedPayload ->
            RealtimeEventType.PERSONALIZATION_PURCHASE_COMPLETED

        is AccountStatusChangedPayload ->
            when (status) {
                "ACTIVE" -> RealtimeEventType.ACCOUNT_ACTIVATED
                "DEACTIVATED" -> RealtimeEventType.ACCOUNT_DEACTIVATED
                "DELETED" -> RealtimeEventType.ACCOUNT_DELETED
                else -> throw IllegalArgumentException("Unsupported account status: $status")
            }

        is AccountOwnershipTransferredPayload ->
            RealtimeEventType.ACCOUNT_OWNER_TRANSFERRED

        is AccountMemberChangedPayload ->
            when (change) {
                "JOINED" -> RealtimeEventType.ACCOUNT_MEMBER_JOINED
                "LEFT" -> RealtimeEventType.ACCOUNT_MEMBER_LEFT
                "REMOVED" -> RealtimeEventType.ACCOUNT_MEMBER_REMOVED
                else -> throw IllegalArgumentException("Unsupported account membership change: $change")
            }
    }
