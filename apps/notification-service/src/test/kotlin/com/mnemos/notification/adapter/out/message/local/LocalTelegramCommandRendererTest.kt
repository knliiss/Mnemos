package com.mnemos.notification.adapter.out.message.local

import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import com.mnemos.notification.application.telegram.help.TelegramHelpSection
import com.mnemos.notification.application.telegram.start.TelegramStartContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class LocalTelegramCommandRendererTest {
    private val renderer =
        LocalNotificationMessageRenderer(
            resolver = StandardMessageResolver(),
            personalizedResolver = PersonalizedMessageResolver(),
            activeNotificationPackRepository = mock<ActiveNotificationPackRepository>(),
        )

    @Test
    fun `start explains mini app launch and exposes web app and help actions`() {
        val message =
            renderer.renderStart(
                TelegramStartContext(
                    telegramId = 123L,
                    firstName = "Knalis",
                    username = "knalis",
                ),
            )

        assertTrue(message.text.contains("Как открыть Mini App"))
        assertTrue(message.actions.any { action -> action is MessageAction.OpenMiniApp })
        assertTrue(
            message.actions.any { action ->
                action is MessageAction.Callback &&
                    action.data == TelegramHelpSection.OVERVIEW.callbackData
            },
        )
    }

    @Test
    fun `every help section renders faq and keeps navigation actions`() {
        TelegramHelpSection.entries.forEach { section ->
            val message = renderer.renderHelp(section)
            val callbacks = message.actions.filterIsInstance<MessageAction.Callback>()

            assertTrue(message.text.contains("<b>"))
            assertEquals(TelegramHelpSection.entries.size, callbacks.size)
            assertTrue(message.actions.first() is MessageAction.OpenMiniApp)
            assertTrue(callbacks.any { action -> action.data == section.callbackData })
        }
    }
}
