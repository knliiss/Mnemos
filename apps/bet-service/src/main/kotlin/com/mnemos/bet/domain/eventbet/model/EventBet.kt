package com.mnemos.bet.domain.eventbet.model

import com.mnemos.common.core.model.id.AccountId

class EventBet private constructor(
    val id: EventBetId,
    val roundId: EventBetRoundId,
    val accountId: AccountId,
    val side: DomainBetSide,
    confirmedAmount: Long,
    pendingAmount: Long,
    status: EventBetStatus,
) {
    var confirmedAmount = confirmedAmount
        private set

    var pendingAmount = pendingAmount
        private set

    var status = status
        private set

    companion object {
        fun create(
            roundId: EventBetRoundId,
            accountId: AccountId,
            side: DomainBetSide,
            amount: Long,
        ): EventBet {
            require(amount > 0)

            return EventBet(
                id = EventBetId.generate(),
                roundId = roundId,
                accountId = accountId,
                side = side,
                confirmedAmount = 0,
                pendingAmount = amount,
                status = EventBetStatus.ACTIVE,
            )
        }

        fun restore(
            id: EventBetId,
            roundId: EventBetRoundId,
            accountId: AccountId,
            side: DomainBetSide,
            confirmedAmount: Long,
            pendingAmount: Long,
            status: EventBetStatus,
        ): EventBet =
            EventBet(
                id = id,
                roundId = roundId,
                accountId = accountId,
                side = side,
                confirmedAmount = confirmedAmount,
                pendingAmount = pendingAmount,
                status = status,
            )
    }

    fun increasePending(
        amount: Long,
        maximumTotal: Long,
    ) {
        require(amount > 0)
        check(confirmedAmount + pendingAmount + amount <= maximumTotal) {
            "Maximum bet amount exceeded"
        }

        pendingAmount += amount
    }

    fun confirmStake(amount: Long) {
        require(amount > 0)
        check(pendingAmount >= amount)

        pendingAmount -= amount
        confirmedAmount += amount
    }

    fun failStake(amount: Long) {
        require(amount > 0)
        check(pendingAmount >= amount)

        pendingAmount -= amount
    }

    fun rejectLateStake(amount: Long) {
        require(amount > 0)
        check(pendingAmount >= amount)

        pendingAmount -= amount
    }

    fun preparePayout() {
        if (confirmedAmount > 0) {
            status = EventBetStatus.SETTLEMENT_PENDING
        }
    }

    fun completePayout() {
        status = EventBetStatus.SETTLED
    }

    fun failPayout() {
        status = EventBetStatus.SETTLEMENT_FAILED
    }

    fun settleWithoutPayout() {
        if (status == EventBetStatus.ACTIVE) {
            status = EventBetStatus.SETTLED
        }
    }

    fun prepareRefund() {
        if (confirmedAmount > 0) {
            status = EventBetStatus.REFUND_PENDING
        } else {
            status = EventBetStatus.REFUNDED
        }
    }

    fun completeRefund() {
        status = EventBetStatus.REFUNDED
    }

    fun failRefund() {
        status = EventBetStatus.REFUND_FAILED
    }
}
