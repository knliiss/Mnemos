package com.mnemos.bet.domain.itemrace.model

import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

class ItemRace private constructor(
    val id: ItemRaceId,
    val itemName: String,
    val itemType: DomainItemType,
    val registrationClosesAt: Instant,
    val startsAt: Instant,
    val expiresAt: Instant,
    val entryAmount: Long,
    val feePercent: Int,
    status: ItemRaceStatus,
    winnerAccountId: AccountId?,
) {
    var status = status
        private set

    var winnerAccountId = winnerAccountId
        private set

    companion object {
        const val FEE_PERCENT = 5

        fun create(
            itemName: String,
            itemType: DomainItemType,
            registrationClosesAt: Instant,
            startsAt: Instant,
            expiresAt: Instant,
            entryAmount: Long,
        ): ItemRace {
            require(itemName.isNotBlank())
            require(!startsAt.isBefore(registrationClosesAt))
            require(expiresAt.isAfter(startsAt))
            require(entryAmount > 0)

            return ItemRace(
                id = ItemRaceId.generate(),
                itemName = itemName,
                itemType = itemType,
                registrationClosesAt = registrationClosesAt,
                startsAt = startsAt,
                expiresAt = expiresAt,
                entryAmount = entryAmount,
                feePercent = FEE_PERCENT,
                status = ItemRaceStatus.REGISTRATION,
                winnerAccountId = null,
            )
        }

        fun restore(
            id: ItemRaceId,
            itemName: String,
            itemType: DomainItemType,
            registrationClosesAt: Instant,
            startsAt: Instant,
            expiresAt: Instant,
            entryAmount: Long,
            feePercent: Int,
            status: ItemRaceStatus,
            winnerAccountId: AccountId?,
        ): ItemRace =
            ItemRace(
                id = id,
                itemName = itemName,
                itemType = itemType,
                registrationClosesAt = registrationClosesAt,
                startsAt = startsAt,
                expiresAt = expiresAt,
                entryAmount = entryAmount,
                feePercent = feePercent,
                status = status,
                winnerAccountId = winnerAccountId,
            )
    }

    fun requireRegistrationOpen(now: Instant) {
        check(status == ItemRaceStatus.REGISTRATION && now.isBefore(registrationClosesAt)) {
            "Item race registration is closed"
        }
    }

    fun activate() {
        check(status == ItemRaceStatus.REGISTRATION)
        status = ItemRaceStatus.ACTIVE
    }

    fun beginSettlement() {
        check(status == ItemRaceStatus.ACTIVE)
        status = ItemRaceStatus.SETTLING
    }

    fun settle(winnerAccountId: AccountId?) {
        this.winnerAccountId = winnerAccountId
        status = ItemRaceStatus.SETTLED
    }

    fun beginRefund() {
        status = ItemRaceStatus.REFUNDING
    }

    fun completeRefund() {
        status = ItemRaceStatus.REFUNDED
    }

    fun cancel() {
        check(status == ItemRaceStatus.REGISTRATION || status == ItemRaceStatus.ACTIVE) {
            "Item race can be cancelled only before settlement starts"
        }

        status = ItemRaceStatus.CANCELLED
    }
}
