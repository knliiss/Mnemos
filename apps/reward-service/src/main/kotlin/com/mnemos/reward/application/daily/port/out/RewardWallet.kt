package com.mnemos.reward.application.daily.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import java.time.LocalDate
import java.util.UUID

interface RewardWallet {
    fun creditDaily(
        accountId: AccountId,
        currency: Currency,
        amount: Long,
        rewardDate: LocalDate,
    )

    fun creditItemDrop(
        accountId: AccountId,
        amount: Long,
        eventId: UUID,
    )
}
