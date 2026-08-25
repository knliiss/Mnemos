package com.mnemos.bet.application.eventbet.usecase.adminbets

import com.mnemos.bet.application.account.model.AccountSummary
import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.model.EventBetStatus
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class ListAdminEventBetsServiceTest {
    private val reader = mock<EventBetReader>()
    private val accountSummaryProvider = mock<AccountSummaryProvider>()
    private val service =
        ListAdminEventBetsService(
            reader = reader,
            accountSummaryProvider = accountSummaryProvider,
        )

    @Test
    fun `enriches round bets with available account nicknames`() {
        val roundId = EventBetRoundId(UUID.fromString("019c1e23-5000-7000-8000-000000000001"))
        val firstAccountId = AccountId(UUID.fromString("019c1e23-5000-7000-8000-000000000002"))
        val secondAccountId = AccountId(UUID.fromString("019c1e23-5000-7000-8000-000000000003"))
        val firstBet = createBet(roundId, firstAccountId, "019c1e23-5000-7000-8000-000000000004")
        val secondBet = createBet(roundId, secondAccountId, "019c1e23-5000-7000-8000-000000000005")

        whenever(reader.findRoundBets(roundId)).thenReturn(listOf(firstBet, secondBet))
        whenever(accountSummaryProvider.getByIds(eq(setOf(firstAccountId, secondAccountId)))).thenReturn(
            mapOf(
                firstAccountId to
                    AccountSummary(
                        accountId = firstAccountId,
                        nickname = "FirstPlayer",
                    ),
            ),
        )

        val result = service.execute(roundId)

        assertEquals("FirstPlayer", result[0].accountNickname)
        assertNull(result[1].accountNickname)
        assertEquals(firstBet.confirmedAmount, result[0].confirmedAmount)
        assertEquals(secondBet.side, result[1].side)
    }

    private fun createBet(
        roundId: EventBetRoundId,
        accountId: AccountId,
        id: String,
    ): EventBetView =
        EventBetView(
            id = EventBetId(UUID.fromString(id)),
            roundId = roundId,
            accountId = accountId,
            side = DomainBetSide.YES,
            confirmedAmount = 1_500,
            pendingAmount = 250,
            status = EventBetStatus.ACTIVE,
        )
}
