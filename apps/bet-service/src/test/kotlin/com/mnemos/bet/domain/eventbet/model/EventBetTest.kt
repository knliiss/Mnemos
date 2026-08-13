package com.mnemos.bet.domain.eventbet.model

import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventBetTest {
    @Test
    fun `late stake rejection removes pending amount without increasing confirmed amount`() {
        val bet =
            EventBet.create(
                roundId = EventBetRoundId.generate(),
                accountId = AccountId.generate(),
                side = DomainBetSide.YES,
                amount = 500,
            )

        bet.rejectLateStake(500)

        assertEquals(0, bet.pendingAmount)
        assertEquals(0, bet.confirmedAmount)
    }

    @Test
    fun `confirmed stake moves amount from pending to confirmed`() {
        val bet =
            EventBet.create(
                roundId = EventBetRoundId.generate(),
                accountId = AccountId.generate(),
                side = DomainBetSide.NO,
                amount = 1_000,
            )

        bet.confirmStake(1_000)

        assertEquals(0, bet.pendingAmount)
        assertEquals(1_000, bet.confirmedAmount)
    }
}
