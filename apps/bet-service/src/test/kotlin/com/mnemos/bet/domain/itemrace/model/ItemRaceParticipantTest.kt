package com.mnemos.bet.domain.itemrace.model

import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ItemRaceParticipantTest {
    @Test
    fun `late entry rejection does not add amount to race bank`() {
        val participant =
            ItemRaceParticipant.create(
                raceId = ItemRaceId.generate(),
                accountId = AccountId.generate(),
                accountNickname = "Player_1",
                amount = 750,
            )

        participant.rejectLateEntry(750)

        assertEquals(0, participant.pendingAmount)
        assertEquals(0, participant.confirmedAmount)
        assertEquals(ItemRaceParticipantStatus.REFUND_PENDING, participant.status)
    }

    @Test
    fun `confirmed entry becomes active`() {
        val participant =
            ItemRaceParticipant.create(
                raceId = ItemRaceId.generate(),
                accountId = AccountId.generate(),
                accountNickname = "Player_1",
                amount = 750,
            )

        participant.confirmEntry(750)

        assertEquals(0, participant.pendingAmount)
        assertEquals(750, participant.confirmedAmount)
        assertEquals(ItemRaceParticipantStatus.ACTIVE, participant.status)
    }
}
