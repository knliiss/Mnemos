package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.bet.BetMenuContext
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRaceHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.springframework.stereotype.Component

@Component
class BetMenuScreenDataProvider(
    private val accountGateway: AccountGateway,
    private val betGateway: BetGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.BETS

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData {
        val mode = BetMenuContext.mode(session.screenContext)
        val accountReady = hasActiveAccount(session.telegramId)
        val rounds =
            if (accountReady) {
                betGateway.listRounds(session.telegramId)
            } else {
                TelegramBetRoundsResult.AccountRequired
            }
        val races =
            if (accountReady) {
                betGateway.listItemRaces(session.telegramId)
            } else {
                TelegramItemRacesResult.AccountRequired
            }
        val eventHistory =
            if (mode == BetMenuMode.HISTORY) {
                if (accountReady) {
                    betGateway.eventHistory(session.telegramId)
                } else {
                    TelegramBetHistoryResult.AccountRequired
                }
            } else {
                null
            }
        val raceHistory =
            if (mode == BetMenuMode.HISTORY) {
                if (accountReady) {
                    betGateway.itemRaceHistory(session.telegramId)
                } else {
                    TelegramItemRaceHistoryResult.AccountRequired
                }
            } else {
                null
            }

        return TelegramMenuScreenData.Bets(
            BetMenuScreenData(
                mode = mode,
                rounds = rounds,
                eventHistory = eventHistory,
                races = races,
                raceHistory = raceHistory,
                selectedRoundId = BetMenuContext.roundId(session.screenContext),
                selectedRaceId = BetMenuContext.raceId(session.screenContext),
                selectedSide = BetMenuContext.side(session.screenContext),
                feedback = BetMenuContext.feedback(session.screenContext),
                waitingForAmount = isWaitingForAmount(session.pendingInput),
                increasing = BetMenuContext.increasing(session.screenContext),
                operationTracked = BetMenuContext.operationId(session.screenContext) != null,
            ),
        )
    }

    private fun isWaitingForAmount(pendingInput: TelegramPendingInput?): Boolean =
        pendingInput == TelegramPendingInput.BET_PLACE_AMOUNT ||
            pendingInput == TelegramPendingInput.BET_INCREASE_AMOUNT ||
            pendingInput == TelegramPendingInput.BET_RACE_INCREASE_AMOUNT

    private fun hasActiveAccount(telegramId: Long): Boolean =
        when (val result = accountGateway.getContext(telegramId)) {
            is TelegramAccountContextResult.Available -> result.context.activeAccountId != null
            TelegramAccountContextResult.RegistrationRequired -> false
            TelegramAccountContextResult.Unavailable -> false
        }
}
