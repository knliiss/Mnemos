package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.bet.BetMenuContext
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
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
        val history =
            if (mode == BetMenuMode.HISTORY) {
                if (accountReady) {
                    betGateway.history(session.telegramId)
                } else {
                    TelegramBetHistoryResult.AccountRequired
                }
            } else {
                null
            }

        return TelegramMenuScreenData.Bets(
            BetMenuScreenData(
                mode = mode,
                rounds = rounds,
                history = history,
                selectedRoundId = BetMenuContext.roundId(session.screenContext),
                selectedSide = BetMenuContext.side(session.screenContext),
                feedback = BetMenuContext.feedback(session.screenContext),
                waitingForAmount =
                    session.pendingInput == TelegramPendingInput.BET_PLACE_AMOUNT ||
                        session.pendingInput == TelegramPendingInput.BET_INCREASE_AMOUNT,
                increasing = BetMenuContext.increasing(session.screenContext),
            ),
        )
    }

    private fun hasActiveAccount(telegramId: Long): Boolean =
        when (val result = accountGateway.getContext(telegramId)) {
            is TelegramAccountContextResult.Available -> result.context.activeAccountId != null
            TelegramAccountContextResult.RegistrationRequired -> false
            TelegramAccountContextResult.Unavailable -> false
        }
}
