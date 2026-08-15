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
    }
