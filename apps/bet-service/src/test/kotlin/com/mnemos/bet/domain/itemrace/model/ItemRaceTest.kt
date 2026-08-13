package com.mnemos.bet.domain.itemrace.model

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class ItemRaceTest {
    @Test
    fun `settled race cannot be cancelled`() {
        val race = createRace()

        race.activate()
        race.beginSettlement()
        race.settle(winnerAccountId = null)

        assertThrows(IllegalStateException::class.java) {
            race.cancel()
        }
    }

    private fun createRace(): ItemRace {
        val startsAt = Instant.parse("2026-08-14T12:10:30Z")

        return ItemRace.create(
            itemName = "Sword",
            itemType = DomainItemType.SWORD,
            registrationClosesAt = startsAt.minusSeconds(30),
            startsAt = startsAt,
            expiresAt = startsAt.plusSeconds(3_600),
            entryAmount = 1_000,
        )
    }
}
