package com.mnemos.reward.application.itemdrop

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.reward.application.daily.port.out.RewardWallet
import com.mnemos.reward.application.itemdrop.usecase.ProcessItemDropRewardCommand
import com.mnemos.reward.application.itemdrop.usecase.ProcessItemDropRewardService
import com.mnemos.reward.configuration.RewardProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class ProcessItemDropRewardServiceTest {
    @Test
    fun `mythical reward is cheaper than secret reward`() {
        val accountId = AccountId.generate()
        val wallet = RecordingWallet()
        val service =
            ProcessItemDropRewardService(
                accounts = { accountId },
                wallet = wallet,
                properties = RewardProperties(),
            )

        service.process(
            ProcessItemDropRewardCommand(
                eventId = UUID.randomUUID(),
                droppedFor = "Player",
                rarity = ContractItemRarity.MYTHICAL,
            ),
        )

        service.process(
            ProcessItemDropRewardCommand(
                eventId = UUID.randomUUID(),
                droppedFor = "Player",
                rarity = ContractItemRarity.SECRET,
            ),
        )

        assertEquals(
            listOf(75L, 150L),
            wallet.amounts,
        )
    }

    @Test
    fun `drop without Mnemos account is ignored`() {
        val wallet = RecordingWallet()
        val service =
            ProcessItemDropRewardService(
                accounts = { null },
                wallet = wallet,
                properties = RewardProperties(),
            )

        service.process(
            ProcessItemDropRewardCommand(
                eventId = UUID.randomUUID(),
                droppedFor = "Unknown",
                rarity = ContractItemRarity.SECRET,
            ),
        )

        assertEquals(
            emptyList<Long>(),
            wallet.amounts,
        )
    }

    private class RecordingWallet : RewardWallet {
        val amounts = mutableListOf<Long>()

        override fun creditDailyCoin(
            accountId: AccountId,
            amount: Long,
            rewardDate: LocalDate,
        ) {
            amounts += amount
        }

        override fun creditDailyPrime(
            userId: UserId,
            amount: Long,
            rewardDate: LocalDate,
        ) {
            amounts += amount
        }

        override fun creditItemDrop(
            accountId: AccountId,
            amount: Long,
            eventId: UUID,
        ) {
            amounts += amount
        }
    }
}
