package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.bet.BetCallbackData
import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBet
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBetRound
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRace
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRaceHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacePosition
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class BetMenuMessageRendererTest {
    @Test
    fun `tracked operation hides new bet and mode actions until refresh resolves it`() {
        val message =
            BetMenuMessageRenderer.render(
                TelegramMenuScreenData.Bets(
                    BetMenuScreenData(
                        mode = BetMenuMode.ROUNDS,
                        rounds = TelegramBetRoundsResult.Available(emptyList()),
                        eventHistory = null,
                        races = TelegramItemRacesResult.Available(emptyList()),
                        raceHistory = null,
                        selectedRoundId = null,
                        selectedRaceId = null,
                        selectedSide = null,
                        feedback = BetMenuFeedback.PENDING,
                        waitingForAmount = false,
                        increasing = false,
                        operationTracked = true,
                    ),
                ),
            )
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Операция ещё обрабатывается"))
        assertTrue(message.text.contains("Нажмите «Обновить»"))
        assertEquals(listOf(1, 1), message.actionRows.map(List<*>::size))
        assertTrue(callbacks.any { action -> action.data == BetCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertFalse(callbacks.any { action -> action.data == BetCallbackData.ROUNDS })
        assertFalse(callbacks.any { action -> action.data == BetCallbackData.RACES })
        assertFalse(callbacks.any { action -> action.data == BetCallbackData.HISTORY })
        assertFalse(callbacks.any { action -> action.data.startsWith("bet:place:") })
        assertFalse(callbacks.any { action -> action.data.startsWith("bet:race:") })
    }

    @Test
    fun `large round list stays within Telegram message limit and hides non rendered actions`() {
        val rounds = (1..40).map(::round)
        val message =
            BetMenuMessageRenderer.render(
                screenData(
                    mode = BetMenuMode.ROUNDS,
                    rounds = TelegramBetRoundsResult.Available(rounds),
                ),
            )
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val hiddenRound = rounds[8]

        assertTrue(message.text.length <= TELEGRAM_MESSAGE_LIMIT)
        assertTrue(message.text.contains("Показано ближайших <b>8</b> из <b>40</b>"))
        assertTrue(
            callbacks.any { action ->
                action.data == BetCallbackData.place(rounds.first().id, TelegramBetSide.YES)
            },
        )
        assertFalse(
            callbacks.any { action ->
                action.data == BetCallbackData.place(hiddenRound.id, TelegramBetSide.YES)
            },
        )
    }

    @Test
    fun `large race list stays within Telegram message limit`() {
        val races = (1..40).map(::race)
        val message =
            BetMenuMessageRenderer.render(
                screenData(
                    mode = BetMenuMode.RACES,
                    races = TelegramItemRacesResult.Available(races),
                ),
            )

        assertTrue(message.text.length <= TELEGRAM_MESSAGE_LIMIT)
        assertTrue(message.text.contains("Показано <b>8</b> из <b>40</b> активных гонок"))
    }

    @Test
    fun `large history stays within Telegram message limit`() {
        val eventBets = (1..40).map(::eventBet)
        val racePositions = (1..40).map(::racePosition)
        val message =
            BetMenuMessageRenderer.render(
                screenData(
                    mode = BetMenuMode.HISTORY,
                    eventHistory = TelegramBetHistoryResult.Available(eventBets),
                    raceHistory = TelegramItemRaceHistoryResult.Available(racePositions),
                ),
            )

        assertTrue(message.text.length <= TELEGRAM_MESSAGE_LIMIT)
        assertTrue(message.text.contains("Показано последних 8 из 40"))
    }

    @Test
    fun `history renders user statuses without backend ids`() {
        val roundId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val raceId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val eventBet =
            TelegramEventBet(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
                roundId = roundId,
                side = TelegramBetSide.YES,
                confirmedAmount = 250,
                pendingAmount = 50,
                status = "SETTLEMENT_PENDING",
            )
        val racePosition =
            TelegramItemRacePosition(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74"),
                raceId = raceId,
                accountNickname = "knaliz",
                confirmedAmount = 700,
                pendingAmount = 0,
                status = "WON",
            )
        val message =
            BetMenuMessageRenderer.render(
                screenData(
                    mode = BetMenuMode.HISTORY,
                    eventHistory = TelegramBetHistoryResult.Available(listOf(eventBet)),
                    raceHistory = TelegramItemRaceHistoryResult.Available(listOf(racePosition)),
                ),
            )

        assertTrue(message.text.contains("Статус: <b>ожидает результата</b>"))
        assertTrue(message.text.contains("Статус: <b>выигрыш</b>"))
        assertTrue(message.text.contains("ожидает подтверждения: 50 COIN"))
        assertFalse(message.text.contains(roundId.toString().take(8)))
        assertFalse(message.text.contains(raceId.toString().take(8)))
        assertFalse(message.text.contains("SETTLEMENT_PENDING"))
        assertFalse(message.text.contains("WON"))
    }

    private fun screenData(
        mode: BetMenuMode,
        rounds: TelegramBetRoundsResult = TelegramBetRoundsResult.Available(emptyList()),
        eventHistory: TelegramBetHistoryResult? = null,
        races: TelegramItemRacesResult = TelegramItemRacesResult.Available(emptyList()),
        raceHistory: TelegramItemRaceHistoryResult? = null,
    ): TelegramMenuScreenData.Bets =
        TelegramMenuScreenData.Bets(
            BetMenuScreenData(
                mode = mode,
                rounds = rounds,
                eventHistory = eventHistory,
                races = races,
                raceHistory = raceHistory,
                selectedRoundId = null,
                selectedRaceId = null,
                selectedSide = null,
                feedback = null,
                waitingForAmount = false,
                increasing = false,
            ),
        )

    private fun round(index: Int): TelegramEventBetRound {
        val scheduledAt = BASE_TIME.plus(index.toLong(), ChronoUnit.HOURS)

        return TelegramEventBetRound(
            id = uuid("round-$index"),
            eventType = if (index % 2 == 0) "ECLIPSE" else "MOON",
            scheduledAt = scheduledAt,
            closesAt = scheduledAt.minus(5, ChronoUnit.MINUTES),
            resolvesAt = scheduledAt.plus(10, ChronoUnit.MINUTES),
            yesOdds = BigDecimal("1.95"),
            noOdds = BigDecimal("2.05"),
            status = "OPEN",
            outcome = null,
            myBet = null,
        )
    }

    private fun race(index: Int): TelegramItemRace {
        val startsAt = BASE_TIME.plus(index.toLong(), ChronoUnit.HOURS)

        return TelegramItemRace(
            id = uuid("race-$index"),
            itemName = "Sword Art Item $index",
            itemType = "WEAPON",
            registrationClosesAt = startsAt.minus(5, ChronoUnit.MINUTES),
            startsAt = startsAt,
            expiresAt = startsAt.plus(1, ChronoUnit.HOURS),
            entryAmount = 100,
            feePercent = 5,
            status = "REGISTRATION",
            winnerAccountId = null,
            myPosition = null,
        )
    }

    private fun eventBet(index: Int): TelegramEventBet =
        TelegramEventBet(
            id = uuid("event-bet-$index"),
            roundId = uuid("event-round-$index"),
            side = TelegramBetSide.YES,
            confirmedAmount = 100L + index,
            pendingAmount = 0,
            status = "SETTLED",
        )

    private fun racePosition(index: Int): TelegramItemRacePosition =
        TelegramItemRacePosition(
            id = uuid("position-$index"),
            raceId = uuid("position-race-$index"),
            accountNickname = "player_$index",
            confirmedAmount = 200L + index,
            pendingAmount = 0,
            status = "WON",
        )

    private fun uuid(value: String): UUID = UUID.nameUUIDFromBytes(value.toByteArray())

    private companion object {
        const val TELEGRAM_MESSAGE_LIMIT = 4096
        val BASE_TIME: Instant = Instant.parse("2026-09-02T12:00:00Z")
    }
}
