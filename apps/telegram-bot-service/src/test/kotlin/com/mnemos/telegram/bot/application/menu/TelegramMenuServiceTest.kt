package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TelegramMenuServiceTest {
    private val renderer = TestMenuMessageRenderer()
    private val screenDataResolver = TelegramMenuScreenDataResolver(emptyList())
    private val messageGateway = TestTelegramMessageGateway()
    private val sessionRepository = TestTelegramMenuSessionRepository()
    private val recipientGateway = TestNotificationRecipientGateway()

    private val service =
        TelegramMenuService(
            renderer = renderer,
            screenDataResolver = screenDataResolver,
            messageGateway = messageGateway,
            sessionRepository = sessionRepository,
            recipientGateway = recipientGateway,
        )

    @Test
    fun `opening first menu sends message and stores message id`() {
        val result = service.open(TELEGRAM_ID)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        assertEquals(listOf(TELEGRAM_ID), recipientGateway.activatedTelegramIds)
        assertEquals(1, messageGateway.sentMessages.size)
        assertEquals(MENU_MESSAGE_ID, sessionRepository.find(TELEGRAM_ID)?.menuMessageId)
        assertEquals(TelegramMenuScreen.HOME, sessionRepository.find(TELEGRAM_ID)?.currentScreen)
    }

    @Test
    fun `navigation and back reuse same menu message`() {
        sessionRepository.save(
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                menuMessageId = MENU_MESSAGE_ID,
            ),
        )

        val navigationResult =
            service.navigate(
                telegramId = TELEGRAM_ID,
                messageId = MENU_MESSAGE_ID,
                screen = TelegramMenuScreen.ONLINE,
            )
        val navigatedSession = sessionRepository.find(TELEGRAM_ID)

        assertEquals(TelegramDeliveryResult.Delivered, navigationResult)
        assertEquals(TelegramMenuScreen.ONLINE, navigatedSession?.currentScreen)
        assertEquals(listOf(TelegramMenuScreen.HOME), navigatedSession?.navigationStack)

        val backResult =
            service.back(
                telegramId = TELEGRAM_ID,
                messageId = MENU_MESSAGE_ID,
            )
        val restoredSession = sessionRepository.find(TELEGRAM_ID)

        assertEquals(TelegramDeliveryResult.Delivered, backResult)
        assertEquals(TelegramMenuScreen.HOME, restoredSession?.currentScreen)
        assertEquals(emptyList<TelegramMenuScreen>(), restoredSession?.navigationStack)
        assertEquals(2, messageGateway.editedMessages.size)
    }

    @Test
    fun `deleted stored menu falls back to new tracked message`() {
        sessionRepository.save(
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                menuMessageId = OLD_MENU_MESSAGE_ID,
            ),
        )
        messageGateway.editResult =
            TelegramDeliveryResult.PermanentFailure(
                reason = "message not found",
            )

        val result = service.open(TELEGRAM_ID)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        assertEquals(1, messageGateway.sentMessages.size)
        assertEquals(MENU_MESSAGE_ID, sessionRepository.find(TELEGRAM_ID)?.menuMessageId)
    }

    private class TestMenuMessageRenderer : TelegramMenuMessageRenderer {
        override fun render(
            screen: TelegramMenuScreen,
            data: TelegramMenuScreenData,
        ): OutboundTelegramMessage =
            OutboundTelegramMessage(
                text = screen.name,
                parseMode = OutboundTelegramMessageParseMode.PLAIN,
                actions = emptyList(),
            )
    }

    private class TestTelegramMenuSessionRepository : TelegramMenuSessionRepository {
        private val sessions = mutableMapOf<Long, TelegramMenuSession>()

        override fun find(telegramId: Long): TelegramMenuSession? = sessions[telegramId]

        override fun save(session: TelegramMenuSession) {
            sessions[session.telegramId] = session
        }
    }

    private class TestNotificationRecipientGateway : NotificationRecipientGateway {
        val activatedTelegramIds = mutableListOf<Long>()

        override fun activate(telegramId: Long) {
            activatedTelegramIds += telegramId
        }
    }

    private class TestTelegramMessageGateway : TelegramMessageGateway {
        val sentMessages = mutableListOf<OutboundTelegramMessage>()
        val editedMessages = mutableListOf<OutboundTelegramMessage>()

        var editResult: TelegramDeliveryResult = TelegramDeliveryResult.Delivered

        override fun sendTracked(
            telegramId: Long,
            message: OutboundTelegramMessage,
        ): TelegramMessageSendResult {
            sentMessages += message
            return TelegramMessageSendResult.Delivered(MENU_MESSAGE_ID)
        }

        override fun edit(
            telegramId: Long,
            messageId: Int,
            message: OutboundTelegramMessage,
        ): TelegramDeliveryResult {
            editedMessages += message
            return editResult
        }
    }

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val OLD_MENU_MESSAGE_ID = 41
        const val MENU_MESSAGE_ID = 42
    }
}
