package com.mnemos.bet.application.eventbet.usecase.list

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import com.mnemos.bet.domain.eventbet.model.EventBetStatus
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ListEventBetRoundsServiceTest {
    private val reader = mock<EventBetReader>()
    private val accountProvider = mock<AccountProvider>()
    private val service =
        ListEventBetRoundsService(
            reader = reader,
            accountProvider = accountProvider,
        )

    @Test
    fun `current rounds include active account bets`() {
        val accountId = AccountId(UUID.fromString("019c1e23-4000-7000-8000-000000000001"))
        val firstRound = createRound("019c1e23-4000-7000-8000-000000000002")
        val secondRound = createRound("019c1e23-4000-7000-8000-000000000003")
        val firstBet =
            EventBetView(
                id = EventBetId(UUID.fromString("019c1e23-4000-7000-8000-000000000004")),
                roundId = firstRound.id,
                accountId = accountId,
                side = DomainBetSide.YES,
                confirmedAmount = 500,
                pendingAmount = 0,
                status = EventBetStatus.ACTIVE,
            )

        whenever(accountProvider.requireActiveAccount()).thenReturn(
            AccountSnapshot(
                id = accountId,
                nickname = "knaliz",
            ),
        )
        whenever(reader.findCurrentRounds()).thenReturn(listOf(firstRound, secondRound))
        whenever(
            reader.findBets(
                roundIds = eq(listOf(firstRound.id, secondRound.id)),
                accountId = eq(accountId),
            ),
        ).thenReturn(listOf(firstBet))

        val result = service.execute()

        assertEquals(2, result.size)
        assertEquals(firstBet, result[0].myBet)
        assertNull(result[1].myBet)
        verify(reader).findBets(
            roundIds = eq(listOf(firstRound.id, secondRound.id)),
            accountId = eq(accountId),
        )
    }

    private fun createRound(id: String): EventBetRoundView =
        EventBetRoundView(
            id = EventBetRoundId(UUID.fromString(id)),
            eventType = DomainBetGlobalEventType.MOON,
            scheduledAt = Instant.parse("2026-08-23T01:00:00Z"),
            closesAt = Instant.parse("2026-08-23T00:55:00Z"),
            resolvesAt = Instant.parse("2026-08-23T03:00:00Z"),
            yesOdds = BigDecimal("1.90"),
            noOdds = BigDecimal("1.90"),
            status = EventBetRoundStatus.OPEN,
            effectiveOutcome = null,
        )
}
