package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetOperationState
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.port.out.BetGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class BetCallbackHandlerGuardTest {
    private val gateway = mock<BetGateway>()
    private val operationResultResolver = mock<BetOperationResultResolver>()
    private val menuService = mock<TelegramMenuService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()

    private val handler =
        BetCallbackHandler(
            gateway = gateway,
            operationResultResolver = operationResultResolver,
            menuService = menuService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `round selection from stale keyboard is ignored in races mode`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(BetMenuContext.races()))

        val result =
            handler.handle(
                callback(BetCallbackData.place(ROUND_ID, TelegramBetSide.YES)),
            )

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, operationResultResolver, menuService)
    }

    @Test
    fun `race selection from stale keyboard is ignored in rounds mode`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(BetMenuContext.rounds()))

        val result = handler.handle(callback(BetCallbackData.joinRace(RACE_ID)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, operationResultResolver, menuService)
    }

    @Test
    fun `pending amount input blocks mode switches and stale selections`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            session(
                screenContext =
                    BetMenuContext.requestPlace(
                        roundId = ROUND_ID,
                        side = TelegramBetSide.YES,
                        idempotencySeed = "seed",
                    ),
                pendingInput = TelegramPendingInput.BET_PLACE_AMOUNT,
            ),
        )

        val modeResult = handler.handle(callback(BetCallbackData.RACES))
        val selectionResult = handler.handle(callback(BetCallbackData.increase(ROUND_ID)))

        assertEquals(TelegramDeliveryResult.Delivered, modeResult)
        assertEquals(TelegramDeliveryResult.Delivered, selectionResult)
        verifyNoInteractions(gateway, operationResultResolver, menuService)
    }

    @Test
    fun `cancel callback without pending bet input is ignored`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(BetMenuContext.rounds()))

        val result = handler.handle(callback(BetCallbackData.CANCEL_INPUT))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, operationResultResolver, menuService)
    }

    @Test
    fun `tracked operation blocks new selections and mode switches`() {
        val trackedContext =
            BetMenuContext.operationResult(
                mode = BetMenuMode.ROUNDS,
                operationId = OPERATION_ID,
                state = TelegramBetOperationState.PENDING,
            )
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(trackedContext))

        val modeResult = handler.handle(callback(BetCallbackData.RACES))
        val selectionResult =
            handler.handle(
                callback(BetCallbackData.place(ROUND_ID, TelegramBetSide.NO)),
            )

        assertEquals(TelegramDeliveryResult.Delivered, modeResult)
        assertEquals(TelegramDeliveryResult.Delivered, selectionResult)
        verifyNoInteractions(gateway, operationResultResolver, menuService)
    }

    @Test
    fun `refresh checks tracked operation instead of replacing its context`() {
        val trackedContext =
            BetMenuContext.operationResult(
                mode = BetMenuMode.ROUNDS,
                operationId = OPERATION_ID,
                state = TelegramBetOperationState.PENDING,
            )
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(trackedContext))
        whenever(gateway.getOperationState(TELEGRAM_ID, OPERATION_ID)).thenReturn(TelegramBetOperationState.COMPLETED)
        whenever(
            menuService.updateScreenContext(
                telegramId = eq(TELEGRAM_ID),
                messageId = eq(MESSAGE_ID),
                expectedScreen = eq(TelegramMenuScreen.BETS),
                update = any(),
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback(BetCallbackData.REFRESH))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(gateway).getOperationState(TELEGRAM_ID, OPERATION_ID)
        verify(menuService).updateScreenContext(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.BETS),
            update = any(),
        )
        verifyNoInteractions(operationResultResolver)
    }

    private fun session(
        screenContext: Map<String, String>,
        pendingInput: TelegramPendingInput? = null,
    ): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = MESSAGE_ID,
            currentScreen = TelegramMenuScreen.BETS,
            pendingInput = pendingInput,
            screenContext = screenContext,
        )

    private fun callback(data: String): TelegramCallbackContext =
        TelegramCallbackContext(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            callbackQueryId = "callback-query",
            data = data,
        )

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val MESSAGE_ID = 42

        val ROUND_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val RACE_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val OPERATION_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73")
    }
}
