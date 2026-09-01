package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetMutationResult
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
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
    private val operationResultResolver: BetOperationResultResolver,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = BetCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentBetsSession(context) ?: return TelegramDeliveryResult.Delivered

        if (isWaitingForBetAmount(session.pendingInput)) {
            return if (context.data == BetCallbackData.CANCEL_INPUT) {
                cancelInput(context, session)
            } else {
                TelegramDeliveryResult.Delivered
            }
        }

        if (BetMenuContext.operationId(session.screenContext) != null) {
            return if (context.data == BetCallbackData.REFRESH) {
                refreshCurrentMode(context, session)
            } else {
                TelegramDeliveryResult.Delivered
            }
        }

        return when (context.data) {
            BetCallbackData.REFRESH -> refreshCurrentMode(context, session)
            BetCallbackData.HISTORY -> update(context, BetMenuContext.history())
            BetCallbackData.ROUNDS -> update(context, BetMenuContext.rounds())
            BetCallbackData.RACES -> update(context, BetMenuContext.races())
            BetCallbackData.CANCEL_INPUT -> TelegramDeliveryResult.Delivered
            else -> handleSelection(context, session)
        }
    }

    private fun handleSelection(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val mode = BetMenuContext.mode(session.screenContext)

        BetCallbackData.placeSelection(context.data)?.let { selection ->
            if (mode != BetMenuMode.ROUNDS) {
                return TelegramDeliveryResult.Delivered
            }

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
            if (mode != BetMenuMode.ROUNDS) {
                return TelegramDeliveryResult.Delivered
            }

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
            if (mode != BetMenuMode.RACES) {
                return TelegramDeliveryResult.Delivered
            }

            return joinRace(context, raceId)
        }

        BetCallbackData.increasedRaceId(context.data)?.let { raceId ->
            if (mode != BetMenuMode.RACES) {
                return TelegramDeliveryResult.Delivered
            }

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
        val result = gateway.joinItemRace(context.telegramId, raceId, idempotencyKey)

        return when (result) {
            is TelegramBetMutationResult.Accepted ->
                update(
                    context = context,
                    screenContext =
                        operationResultResolver.resolve(
                            telegramId = context.telegramId,
                            mode = BetMenuMode.RACES,
                            operationId = result.operationId,
                        ),
                )

            TelegramBetMutationResult.AccountRequired ->
                update(context, BetMenuContext.accountRequired(BetMenuMode.RACES))

            TelegramBetMutationResult.Invalid ->
                update(
                    context,
                    BetMenuContext.withFeedback(BetMenuContext.races(), BetMenuFeedback.INVALID_AMOUNT),
                )

            TelegramBetMutationResult.Unavailable ->
                update(
                    context,
                    BetMenuContext.withFeedback(BetMenuContext.races(), BetMenuFeedback.UNAVAILABLE),
                )
        }
    }

    private fun refreshCurrentMode(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val mode = BetMenuContext.mode(session.screenContext)
        val operationId = BetMenuContext.operationId(session.screenContext)
        val screenContext =
            if (operationId == null) {
                BetMenuContext.refresh(session.screenContext, mode)
            } else {
                BetMenuContext.operationResult(
                    mode = mode,
                    operationId = operationId,
                    state = gateway.getOperationState(context.telegramId, operationId),
                )
            }

        return update(
            context = context,
            screenContext = screenContext,
        )
    }

    private fun cancelInput(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val mode = BetMenuContext.mode(session.screenContext)

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

    private fun currentBetsSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.BETS || session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }

    private fun isWaitingForBetAmount(pendingInput: TelegramPendingInput?): Boolean =
        pendingInput == TelegramPendingInput.BET_PLACE_AMOUNT ||
            pendingInput == TelegramPendingInput.BET_INCREASE_AMOUNT ||
            pendingInput == TelegramPendingInput.BET_RACE_INCREASE_AMOUNT

    private fun deterministicKey(value: String): UUID =
        UUID.nameUUIDFromBytes(
            "mnemos:telegram-bet:$value".toByteArray(StandardCharsets.UTF_8),
        )
}
