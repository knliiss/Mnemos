package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalTelegramCommandMessageRendererTest {
    private val renderer = LocalTelegramCommandMessageRenderer()

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
        assertTrue(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
        assertTrue(
            message.actions.any { action ->
                action is OutboundTelegramMessageAction.Callback &&
                    action.data == TelegramHelpSection.OVERVIEW.callbackData
            },
        )
    }

    @Test
    fun `every help section renders faq and keeps navigation actions`() {
        TelegramHelpSection.entries.forEach { section ->
            val message = renderer.renderHelp(section)
            val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

            assertTrue(message.text.contains("<b>"))
            assertEquals(TelegramHelpSection.entries.size, callbacks.size)
            assertTrue(message.actions.first() is OutboundTelegramMessageAction.OpenMiniApp)
            assertTrue(callbacks.any { action -> action.data == section.callbackData })
        }
    }
}
