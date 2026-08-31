package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalTelegramCommandMessageRendererTest {
    private val renderer = LocalTelegramCommandMessageRenderer()

    @Test
    fun `start presents native menu first and keeps mini app optional`() {
        val message =
            renderer.renderStart(
                TelegramStartContext(
                    telegramId = 123L,
                    firstName = "Knalis",
                    username = "knalis",
                ),
            )

        assertTrue(message.text.contains("Mini App для этого не нужен"))
        assertTrue(message.text.contains("Mini App ниже остаётся дополнительным интерфейсом"))
        assertTrue(
            message.actions.any { action ->
                action is OutboundTelegramMessageAction.Callback &&
                    action.data == TelegramMenuScreen.HOME.callbackData
            },
        )
        assertTrue(
            message.actions.any { action ->
                action is OutboundTelegramMessageAction.Callback &&
                    action.data == TelegramHelpSection.OVERVIEW.callbackData
            },
        )
        assertTrue(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
    }

    @Test
    fun `every help section keeps native menu help navigation and optional mini app`() {
        TelegramHelpSection.entries.forEach { section ->
            val message = renderer.renderHelp(section)
            val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

            assertTrue(message.text.contains("<b>"))
            assertEquals(TelegramHelpSection.entries.size + 1, callbacks.size)
            assertTrue(
                callbacks.any { action ->
                    action.data == TelegramMenuScreen.HOME.callbackData
                },
            )
            assertTrue(callbacks.any { action -> action.data == section.callbackData })
            assertTrue(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
        }
    }
}
