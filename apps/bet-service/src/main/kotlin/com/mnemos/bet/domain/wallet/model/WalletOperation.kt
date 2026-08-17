package com.mnemos.bet.domain.wallet.model

import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolutionId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.time.Instant
import java.util.UUID

class WalletOperation private constructor(
    val id: WalletOperationId,
    val requestId: WalletRequestId,
    val accountId: AccountId,
    val initiatedByUserId: UserId?,
    val amount: Long,
    val kind: WalletOperationKind,
    val requestType: WalletOperationRequestType?,
    val idempotencyKey: UUID?,
    val eventBetId: EventBetId?,
    val eventBetResolutionId: EventBetResolutionId?,
    val raceParticipantId: ItemRaceParticipantId?,
    val createdAt: Instant,
    val eligibleAt: Instant,
    status: WalletOperationStatus,
    walletTransactionId: WalletTransactionId?,
) {
    var status: WalletOperationStatus = status
        private set

    var walletTransactionId: WalletTransactionId? = walletTransactionId
        private set

    companion object {
        fun create(
            accountId: AccountId,
            initiatedByUserId: UserId? = null,
            amount: Long,
            kind: WalletOperationKind,
            requestType: WalletOperationRequestType? = null,
            idempotencyKey: UUID? = null,
            eventBetId: EventBetId?,
            eventBetResolutionId: EventBetResolutionId?,
            raceParticipantId: ItemRaceParticipantId?,
            createdAt: Instant,
            eligibleAt: Instant,
        ): WalletOperation {
            require(amount > 0)
            require((eventBetId != null) xor (raceParticipantId != null))
            require((requestType == null) == (idempotencyKey == null))
            require(requestType == null || kind == WalletOperationKind.STAKE)

            return WalletOperation(
                id = WalletOperationId.generate(),
                requestId = WalletRequestId.generate(),
                accountId = accountId,
                initiatedByUserId = initiatedByUserId,
                amount = amount,
                kind = kind,
                requestType = requestType,
                idempotencyKey = idempotencyKey,
                eventBetId = eventBetId,
                eventBetResolutionId = eventBetResolutionId,
                raceParticipantId = raceParticipantId,
                createdAt = createdAt,
                eligibleAt = eligibleAt,
                status = WalletOperationStatus.PENDING,
                walletTransactionId = null,
            )
        }

        fun restore(
            id: WalletOperationId,
            requestId: WalletRequestId,
            accountId: AccountId,
            initiatedByUserId: UserId?,
            amount: Long,
            kind: WalletOperationKind,
            requestType: WalletOperationRequestType? = null,
            idempotencyKey: UUID? = null,
            eventBetId: EventBetId?,
            eventBetResolutionId: EventBetResolutionId?,
            raceParticipantId: ItemRaceParticipantId?,
            createdAt: Instant,
            eligibleAt: Instant,
            status: WalletOperationStatus,
            walletTransactionId: WalletTransactionId?,
        ): WalletOperation =
            WalletOperation(
                id = id,
                requestId = requestId,
                accountId = accountId,
                initiatedByUserId = initiatedByUserId,
                amount = amount,
                kind = kind,
                requestType = requestType,
                idempotencyKey = idempotencyKey,
                eventBetId = eventBetId,
                eventBetResolutionId = eventBetResolutionId,
                raceParticipantId = raceParticipantId,
                createdAt = createdAt,
                eligibleAt = eligibleAt,
                status = status,
                walletTransactionId = walletTransactionId,
            )
    }

    fun retry(createdAt: Instant): WalletOperation {
        check(status == WalletOperationStatus.FAILED)
        check(kind == WalletOperationKind.PAYOUT || kind == WalletOperationKind.REFUND)

        return WalletOperation(
            id = WalletOperationId.generate(),
            requestId = WalletRequestId.generate(),
            accountId = accountId,
            initiatedByUserId = initiatedByUserId,
            amount = amount,
            kind = kind,
            requestType = null,
            idempotencyKey = null,
            eventBetId = eventBetId,
            eventBetResolutionId = eventBetResolutionId,
            raceParticipantId = raceParticipantId,
            createdAt = createdAt,
            eligibleAt = createdAt,
            status = WalletOperationStatus.PENDING,
            walletTransactionId = null,
        )
    }

    fun complete(transactionId: WalletTransactionId) {
        if (status != WalletOperationStatus.PENDING) {
            return
        }

        status = WalletOperationStatus.COMPLETED
        walletTransactionId = transactionId
    }

    fun fail() {
        if (status == WalletOperationStatus.PENDING) {
            status = WalletOperationStatus.FAILED
        }
    }
}
