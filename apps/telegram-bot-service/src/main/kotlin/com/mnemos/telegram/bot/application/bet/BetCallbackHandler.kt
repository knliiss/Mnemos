package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class BetCallbackHandler(
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = BetCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (!isCurrentBetsMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        return when (context.data) {
            BetCallbackData.REFRESH -> update(context, BetMenuContext.rounds())
            BetCallbackData.HISTORY -> update(context, BetMenuContext.history())
            BetCallbackData.ROUNDS -> update(context, BetMenuContext.rounds())
            BetCallbackData.CANCEL_INPUT -> cancelInput(context)
            else -> handleSelection(context)
        }
    }

    private fun handleSelection(context: TelegramCallbackContext): TelegramDeliveryResult {
        BetCallbackData.placeSelection(context.data)?.let { selection ->
            val (roundId, side) = selection

            return menuService.requestTextInput(
                telegramId = context.telegramId,
                messageId = context.messageId,
                expectedScreen = TelegramMenuScreen.BETS,
                pendingInput = TelegramPendingInput.BET_PLACE_AMOUNT,
                updateContext = {
                    BetMenuContext.requestPlace(
                        roundId = roundId,
                        side = side,
                        idempotencySeed = context.callbackQueryId,
                    )
                },
            )
        }

        BetCallbackData.increasedRoundId(context.data)?.let { roundId ->
            return menuService.requestTextInput(
                telegramId = context.telegramId,
                messageId = context.messageId,
                expectedScreen = TelegramMenuScreen.BETS,
                pendingInput = TelegramPendingInput.BET_INCREASE_AMOUNT,
                updateContext = {
                    BetMenuContext.requestIncrease(
                        roundId = roundId,
                        idempotencySeed = context.callbackQueryId,
                    )
                },
            )
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun cancelInput(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.BETS,
            updateContext = { BetMenuContext.rounds() },
        )

    private fun update(
        context: TelegramCallbackContext,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.BETS,
            update = { screenContext },
        )

    private fun isCurrentBetsMenu(context: TelegramCallbackContext): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.BETS && session.menuMessageId == context.messageId
    }
}
