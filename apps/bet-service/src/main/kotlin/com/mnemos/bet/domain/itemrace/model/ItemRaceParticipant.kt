package com.mnemos.bet.domain.itemrace.model

import com.mnemos.common.core.model.id.AccountId

class ItemRaceParticipant private constructor(
    val id: ItemRaceParticipantId,
    val raceId: ItemRaceId,
    val accountId: AccountId,
    val accountNickname: String,
    confirmedAmount: Long,
    pendingAmount: Long,
    status: ItemRaceParticipantStatus,
) {
    var confirmedAmount = confirmedAmount
        private set

    var pendingAmount = pendingAmount
        private set

    var status = status
        private set

    companion object {
        fun create(
            raceId: ItemRaceId,
            accountId: AccountId,
            accountNickname: String,
            amount: Long,
        ): ItemRaceParticipant {
            require(accountNickname.isNotBlank())
            require(amount > 0)

            return ItemRaceParticipant(
                id = ItemRaceParticipantId.generate(),
                raceId = raceId,
                accountId = accountId,
                accountNickname = accountNickname,
                confirmedAmount = 0,
                pendingAmount = amount,
                status = ItemRaceParticipantStatus.PENDING,
            )
        }

        fun restore(
            id: ItemRaceParticipantId,
            raceId: ItemRaceId,
            accountId: AccountId,
            accountNickname: String,
            confirmedAmount: Long,
            pendingAmount: Long,
            status: ItemRaceParticipantStatus,
        ): ItemRaceParticipant =
            ItemRaceParticipant(
                id = id,
                raceId = raceId,
                accountId = accountId,
                accountNickname = accountNickname,
                confirmedAmount = confirmedAmount,
                pendingAmount = pendingAmount,
                status = status,
            )
    }

    fun confirmEntry(amount: Long) {
        require(amount > 0)
        check(pendingAmount >= amount)

        pendingAmount -= amount
        confirmedAmount += amount
        status = ItemRaceParticipantStatus.ACTIVE
    }

    fun failEntry(amount: Long) {
        require(amount > 0)
        check(pendingAmount >= amount)

        pendingAmount -= amount
    }

    fun rejectLateEntry(amount: Long) {
        require(amount > 0)
        check(pendingAmount >= amount)

        pendingAmount -= amount
        status = ItemRaceParticipantStatus.REFUND_PENDING
    }

    fun preparePayout() {
        status = ItemRaceParticipantStatus.PAYOUT_PENDING
    }

    fun completePayout() {
        status = ItemRaceParticipantStatus.WON
    }

    fun failPayout() {
        status = ItemRaceParticipantStatus.PAYOUT_FAILED
    }

    fun lose() {
        status = ItemRaceParticipantStatus.LOST
    }

    fun prepareRefund() {
        status = ItemRaceParticipantStatus.REFUND_PENDING
    }

    fun completeRefund() {
        status = ItemRaceParticipantStatus.REFUNDED
    }

    fun failRefund() {
        status = ItemRaceParticipantStatus.REFUND_FAILED
    }
}
