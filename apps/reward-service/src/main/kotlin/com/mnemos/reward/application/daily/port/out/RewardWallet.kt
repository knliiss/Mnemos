package com.mnemos.reward.application.daily.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.time.LocalDate
import java.util.UUID

interface RewardWallet {
    fun creditDailyCoin(
        accountId: AccountId,
        amount: Long,
        rewardDate: LocalDate,
    )

    fun creditDailyPrime(
        userId: UserId,
        amount: Long,
        rewardDate: LocalDate,
    )

    fun creditItemDrop(
        accountId: AccountId,
        amount: Long,
        eventId: UUID,
    )
}
