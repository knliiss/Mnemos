package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetMutationResult
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class BetCallbackHandler(
    private val gateway: BetGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = BetCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (!isCurrentBetsMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        return when (context.data) {
            BetCallbackData.REFRESH -> refreshCurrentMode(context)
            BetCallbackData.HISTORY -> update(context, BetMenuContext.history())
            BetCallbackData.ROUNDS -> update(context, BetMenuContext.rounds())
            BetCallbackData.RACES -> update(context, BetMenuContext.races())
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

        BetCallbackData.joinedRaceId(context.data)?.let { raceId ->
            return joinRace(context, raceId)
        }

        BetCallbackData.increasedRaceId(context.data)?.let { raceId ->
            return menuService.requestTextInput(
                telegramId = context.telegramId,
                messageId = context.messageId,
                expectedScreen = TelegramMenuScreen.BETS,
                pendingInput = TelegramPendingInput.BET_RACE_INCREASE_AMOUNT,
                updateContext = {
                    BetMenuContext.requestRaceIncrease(
                        raceId = raceId,
                        idempotencySeed = context.callbackQueryId,
                    )
                },
            )
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun joinRace(
        context: TelegramCallbackContext,
        raceId: UUID,
    ): TelegramDeliveryResult {
        val idempotencyKey = deterministicKey("race-join:${context.callbackQueryId}:$raceId")

        return when (gateway.joinItemRace(context.telegramId, raceId, idempotencyKey)) {
            TelegramBetMutationResult.ACCEPTED ->
                update(context, BetMenuContext.accepted(BetMenuMode.RACES))

            TelegramBetMutationResult.ACCOUNT_REQUIRED ->
                update(context, BetMenuContext.accountRequired(BetMenuMode.RACES))

            TelegramBetMutationResult.INVALID ->
                update(
                    context,
                    BetMenuContext.withFeedback(BetMenuContext.races(), BetMenuFeedback.INVALID_AMOUNT),
                )

            TelegramBetMutationResult.UNAVAILABLE ->
                update(
                    context,
                    BetMenuContext.withFeedback(BetMenuContext.races(), BetMenuFeedback.UNAVAILABLE),
                )
        }
    }

    private fun refreshCurrentMode(context: TelegramCallbackContext): TelegramDeliveryResult {
        val mode = sessionRepository.find(context.telegramId)?.screenContext?.let(BetMenuContext::mode) ?: BetMenuMode.ROUNDS
        val screenContext =
            when (mode) {
                BetMenuMode.ROUNDS -> BetMenuContext.rounds()
                BetMenuMode.RACES -> BetMenuContext.races()
                BetMenuMode.HISTORY -> BetMenuContext.history()
            }

        return update(context, screenContext)
    }

    private fun cancelInput(context: TelegramCallbackContext): TelegramDeliveryResult {
        val mode = sessionRepository.find(context.telegramId)?.screenContext?.let(BetMenuContext::mode) ?: BetMenuMode.ROUNDS

        return menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.BETS,
            updateContext = {
                when (mode) {
                    BetMenuMode.ROUNDS -> BetMenuContext.rounds()
                    BetMenuMode.RACES -> BetMenuContext.races()
                    BetMenuMode.HISTORY -> BetMenuContext.history()
                }
            },
        )
    }

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

    private fun deterministicKey(value: String): UUID =
        UUID.nameUUIDFromBytes(
            "mnemos:telegram-bet:$value".toByteArray(StandardCharsets.UTF_8),
        )
}
