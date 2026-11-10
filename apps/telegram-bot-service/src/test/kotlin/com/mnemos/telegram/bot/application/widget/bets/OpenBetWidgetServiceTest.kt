package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBetRound
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.bets.port.out.BetWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OpenBetWidgetServiceTest {
    private val accounts = mock<AccountGateway>()
    private val bets = mock<BetGateway>()
    private val publisher = mock<BetWidgetPublisher>()

    @Test
    fun disabledBetWidgetsNeverReadFinancialData() {
        val service = OpenBetWidgetService(TelegramWidgetProperties(enabled = false), accounts, bets, publisher)
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            service.execute(11L),
        )
        verifyNoInteractions(accounts, bets, publisher)
    }

    @Test
    fun roundWidgetUsesExistingTypedBetGatewayWithoutMutations() {
        val id = UUID.randomUUID()
        whenever(accounts.getContext(11L)).thenReturn(
            TelegramAccountContextResult.Available(TelegramAccountContext(id, emptyList())),
        )
        whenever(bets.listRounds(11L)).thenReturn(
            TelegramBetRoundsResult.Available(
                listOf(
                    TelegramEventBetRound(
                        id = UUID.randomUUID(),
                        eventType = "GLOBAL",
                        scheduledAt = Instant.parse("2026-11-09T19:00:00Z"),
                        closesAt = Instant.parse("2026-11-09T19:30:00Z"),
                        resolvesAt = Instant.parse("2026-11-09T20:00:00Z"),
                        yesOdds = BigDecimal("1.8"),
                        noOdds = BigDecimal("2.1"),
                        status = "OPEN",
                        outcome = null,
                        myBet = null,
                    ),
                ),
            ),
        )
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        val service = OpenBetWidgetService(TelegramWidgetProperties(enabled = true), accounts, bets, publisher)
        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L))

        val view = argumentCaptor<BetWidgetView>()
        verify(publisher).publish(eq(11L), view.capture())
        assertEquals(id, view.firstValue.accountId)
        assertEquals(BetMenuMode.ROUNDS, view.firstValue.mode)
        assertEquals(1, view.firstValue.totalItems)
        assertTrue(view.firstValue.entries.first().details.contains("YES 1.8"))
    }
}
