package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetMutationResult
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputHandler
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
class BetRaceIncreaseAmountTextInputHandler(
    private val gateway: BetGateway,
    private val operationResultResolver: BetOperationResultResolver,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramTextInputHandler {
    override val pendingInput: TelegramPendingInput = TelegramPendingInput.BET_RACE_INCREASE_AMOUNT

    override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
        val session = sessionRepository.find(context.telegramId) ?: return TelegramDeliveryResult.Delivered
        val raceId = BetMenuContext.raceId(session.screenContext) ?: return TelegramDeliveryResult.Delivered
        val seed = BetMenuContext.idempotencySeed(session.screenContext) ?: return TelegramDeliveryResult.Delivered
        val amount = context.text.trim().toLongOrNull()

        if (amount == null || amount <= 0) {
            return updatePending(
                telegramId = context.telegramId,
                screenContext = BetMenuContext.withFeedback(session.screenContext, BetMenuFeedback.INVALID_AMOUNT),
            )
        }

        val idempotencyKey = deterministicKey("race-increase:$seed:$raceId:$amount")
        val result =
            gateway.increaseItemRace(
                telegramId = context.telegramId,
                raceId = raceId,
                amount = amount,
                idempotencyKey = idempotencyKey,
            )

        return when (result) {
            is TelegramBetMutationResult.Accepted ->
                complete(
                    telegramId = context.telegramId,
                    screenContext =
                        operationResultResolver.resolve(
                            telegramId = context.telegramId,
                            mode = BetMenuMode.RACES,
                            operationId = result.operationId,
                        ),
                )

            TelegramBetMutationResult.AccountRequired ->
                complete(
                    telegramId = context.telegramId,
                    screenContext = BetMenuContext.accountRequired(BetMenuMode.RACES),
                )

            TelegramBetMutationResult.Invalid ->
                updatePending(
                    telegramId = context.telegramId,
                    screenContext = BetMenuContext.withFeedback(session.screenContext, BetMenuFeedback.INVALID_AMOUNT),
                )

            TelegramBetMutationResult.Unavailable ->
                updatePending(
                    telegramId = context.telegramId,
                    screenContext = BetMenuContext.withFeedback(session.screenContext, BetMenuFeedback.UNAVAILABLE),
                )
        }
    }

    private fun complete(
        telegramId: Long,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.completeTextInput(
            telegramId = telegramId,
            expectedScreen = TelegramMenuScreen.BETS,
            expectedInput = pendingInput,
            updateContext = { screenContext },
        )

    private fun updatePending(
        telegramId: Long,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updatePendingTextInputContext(
            telegramId = telegramId,
            expectedScreen = TelegramMenuScreen.BETS,
            expectedInput = pendingInput,
            updateContext = { screenContext },
        )

    private fun deterministicKey(value: String): UUID =
        UUID.nameUUIDFromBytes(
            "mnemos:telegram-bet:$value".toByteArray(StandardCharsets.UTF_8),
        )
}
