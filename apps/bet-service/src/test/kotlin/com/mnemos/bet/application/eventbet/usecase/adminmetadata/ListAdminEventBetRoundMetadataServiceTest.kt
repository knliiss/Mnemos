package com.mnemos.bet.application.eventbet.usecase.adminmetadata

import com.mnemos.bet.application.account.model.AccountSummary
import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ListAdminEventBetRoundMetadataServiceTest {
    private val reader = mock<EventBetReader>()
    private val accountSummaryProvider = mock<AccountSummaryProvider>()
    private val service =
        ListAdminEventBetRoundMetadataService(
            reader = reader,
            accountSummaryProvider = accountSummaryProvider,
        )

    @Test
    fun `returns bettor metadata and keeps rounds without bets visible`() {
        val firstRound = createRound("019c1e23-7000-7000-8000-000000000001")
        val secondRound = createRound("019c1e23-7000-7000-8000-000000000002")
        val accountId = AccountId(UUID.fromString("019c1e23-7000-7000-8000-000000000003"))
        val bet = createBet(firstRound.id, accountId)

        whenever(reader.findAllRounds()).thenReturn(listOf(firstRound, secondRound))
        whenever(reader.findRoundBets(eq(listOf(firstRound.id, secondRound.id)))).thenReturn(listOf(bet))
        whenever(accountSummaryProvider.getByIds(eq(setOf(accountId)))).thenReturn(
            mapOf(
                accountId to
                    AccountSummary(
                        accountId = accountId,
                        nickname = "PlayerOne",
                    ),
            ),
        )

        val result = service.execute()

        assertEquals(2, result.size)
        assertEquals(1, result[0].betCount)
        assertEquals("PlayerOne", result[0].bettors.single().accountNickname)
        assertEquals(accountId, result[0].bettors.single().accountId)
        assertEquals(0, result[1].betCount)
        assertTrue(result[1].bettors.isEmpty())
    }

    private fun createRound(id: String): EventBetRoundView =
        EventBetRoundView(
            id = EventBetRoundId(UUID.fromString(id)),
            eventType = DomainBetGlobalEventType.MOON,
            scheduledAt = Instant.parse("2026-08-24T20:00:00Z"),
            closesAt = Instant.parse("2026-08-24T19:55:00Z"),
            resolvesAt = Instant.parse("2026-08-24T22:00:00Z"),
            yesOdds = BigDecimal("1.90"),
            noOdds = BigDecimal("1.90"),
            status = EventBetRoundStatus.SETTLED,
            effectiveOutcome = null,
        )

    private fun createBet(
        roundId: EventBetRoundId,
        accountId: AccountId,
    ): EventBetView =
        EventBetView(
            id = EventBetId(UUID.fromString("019c1e23-7000-7000-8000-000000000004")),
            roundId = roundId,
            accountId = accountId,
            side = DomainBetSide.YES,
            confirmedAmount = 1_000,
            pendingAmount = 0,
            status = EventBetStatus.SETTLED,
        )
}
