package com.mnemos.bet.domain.eventbet.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class EventBetRoundTest {
    @Test
    fun `create generates time ordered id and opens round`() {
        val round = createRound()

        assertEquals(7, round.id.id.version())
        assertEquals(EventBetRoundStatus.OPEN, round.status)
        assertNull(round.effectiveOutcome)
    }

    @Test
    fun `create generates different ids`() {
        val first = createRound()
        val second = createRound()

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `round waits for admin when automatic result cannot be confirmed`() {
        val round = createRound()
        val scheduledAt = Instant.parse("2026-08-14T12:00:00Z")

        round.synchronize(scheduledAt.plusSeconds(120))
        round.awaitAdministrativeConfirmation()

        assertEquals(EventBetRoundStatus.AWAITING_ADMIN_CONFIRMATION, round.status)
        assertNull(round.effectiveOutcome)
    }

    @Test
    fun `late observed event can resolve round waiting for administrator`() {
        val round = createRound()
        val scheduledAt = Instant.parse("2026-08-14T12:00:00Z")

        round.synchronize(scheduledAt.plusSeconds(120))
        round.awaitAdministrativeConfirmation()
        round.resolveAutomatically(EventBetOutcome.YES)

        assertEquals(EventBetRoundStatus.SETTLED, round.status)
        assertEquals(EventBetOutcome.YES, round.effectiveOutcome)
    }

    @Test
    fun `administrative correction can replace effective outcome`() {
        val round = createRound()

        round.synchronize(Instant.parse("2026-08-14T12:02:00Z"))
        round.resolveAutomatically(EventBetOutcome.NO)
        round.resolveAdministratively(EventBetOutcome.YES)

        assertEquals(EventBetRoundStatus.SETTLED, round.status)
        assertEquals(EventBetOutcome.YES, round.effectiveOutcome)
    }

    @Test
    fun `manual yes cannot resolve an open round`() {
        val round = createRound()

        assertThrows(IllegalStateException::class.java) {
            round.resolveAdministratively(EventBetOutcome.YES)
        }
    }

    @Test
    fun `admin can refund an open round`() {
        val round = createRound()

        round.resolveAdministratively(EventBetOutcome.REFUND)

        assertEquals(EventBetRoundStatus.SETTLED, round.status)
        assertEquals(EventBetOutcome.REFUND, round.effectiveOutcome)
    }

    private fun createRound(): EventBetRound {
        val scheduledAt = Instant.parse("2026-08-14T12:00:00Z")

        return EventBetRound.create(
            eventType = DomainBetGlobalEventType.MOON,
            scheduledAt = scheduledAt,
            closesAt = scheduledAt.minusSeconds(300),
            resolvesAt = scheduledAt.plusSeconds(120),
            yesOdds = BigDecimal("1.9000"),
            noOdds = BigDecimal("1.9000"),
        )
    }
}
