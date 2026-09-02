package com.mnemos.telegram.bot.application.account.invite

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteMutationResult
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
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

class AccountInviteCallbackHandlerTest {
    private val gateway = mock<AccountInviteGateway>()
    private val menuService = mock<TelegramMenuService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()

    private val handler =
        AccountInviteCallbackHandler(
            gateway = gateway,
            menuService = menuService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `create uses expiry stored in the invite screen context`() {
        val screenContext =
            AccountInviteMenuContext.selectExpiry(
                context = emptyMap(),
                expiry = TelegramAccountInviteExpiry.SEVEN_DAYS,
            )
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(screenContext))
        whenever(gateway.createInvite(TELEGRAM_ID, 10_080)).thenReturn(TelegramAccountInviteMutationResult.UPDATED)
        stubMenuUpdate()

        val result = handler.handle(callback(AccountInviteCallbackData.CREATE))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(gateway).createInvite(TELEGRAM_ID, 10_080)
        verify(menuService).updateScreenContext(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.ACCOUNT_INVITES),
            update = any(),
        )
    }

    @Test
    fun `replayed revoke confirmation cannot target another invite`() {
        val selectedInviteId = UUID.randomUUID()
        val replayedInviteId = UUID.randomUUID()
        val screenContext = AccountInviteMenuContext.requestRevoke(emptyMap(), selectedInviteId)
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(screenContext))

        val result = handler.handle(callback(AccountInviteCallbackData.confirmRevoke(replayedInviteId)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `matching revoke confirmation performs one targeted mutation`() {
        val inviteId = UUID.randomUUID()
        val screenContext = AccountInviteMenuContext.requestRevoke(emptyMap(), inviteId)
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(screenContext))
        whenever(gateway.revokeInvite(TELEGRAM_ID, inviteId)).thenReturn(TelegramAccountInviteMutationResult.UPDATED)
        stubMenuUpdate()

        val result = handler.handle(callback(AccountInviteCallbackData.confirmRevoke(inviteId)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(gateway).revokeInvite(TELEGRAM_ID, inviteId)
    }

    private fun stubMenuUpdate() {
        whenever(
            menuService.updateScreenContext(
                telegramId = eq(TELEGRAM_ID),
                messageId = eq(MESSAGE_ID),
                expectedScreen = eq(TelegramMenuScreen.ACCOUNT_INVITES),
                update = any(),
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)
    }

    private fun session(screenContext: Map<String, String>): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = MESSAGE_ID,
            currentScreen = TelegramMenuScreen.ACCOUNT_INVITES,
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
    }
}
