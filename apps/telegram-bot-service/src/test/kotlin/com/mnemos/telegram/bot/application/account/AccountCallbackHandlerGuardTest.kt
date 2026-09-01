package com.mnemos.telegram.bot.application.account

import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
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

class AccountCallbackHandlerGuardTest {
    private val gateway = mock<AccountGateway>()
    private val menuService = mock<TelegramMenuService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()

    private val handler =
        AccountCallbackHandler(
            gateway = gateway,
            menuService = menuService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `pending create input ignores queued account switch`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            session(TelegramPendingInput.ACCOUNT_CREATE),
        )

        val result = handler.handle(callback(AccountCallbackData.switch(ACCOUNT_ID)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `pending account input cannot be replaced by another input mode`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            session(TelegramPendingInput.ACCOUNT_CREATE),
        )

        val result = handler.handle(callback(AccountCallbackData.JOIN))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `cancel without pending account input is ignored`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session())

        val result = handler.handle(callback(AccountCallbackData.CANCEL_INPUT))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `pending account input can still be cancelled`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            session(TelegramPendingInput.ACCOUNT_JOIN),
        )
        whenever(
            menuService.cancelTextInput(
                telegramId = eq(TELEGRAM_ID),
                messageId = eq(MESSAGE_ID),
                expectedScreen = eq(TelegramMenuScreen.ACCOUNT),
                updateContext = any(),
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback(AccountCallbackData.CANCEL_INPUT))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).cancelTextInput(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.ACCOUNT),
            updateContext = any(),
        )
        verifyNoInteractions(gateway)
    }

    private fun session(pendingInput: TelegramPendingInput? = null): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = MESSAGE_ID,
            currentScreen = TelegramMenuScreen.ACCOUNT,
            pendingInput = pendingInput,
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

        val ACCOUNT_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
    }
}
