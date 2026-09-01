package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalTelegramCommandMessageRendererTest {
    private val renderer = LocalTelegramCommandMessageRenderer()

    @Test
    fun `start presents native actions together and keeps mini app optional`() {
        val message = renderer.renderStart(startContext(), TelegramSettingsLanguage.RU)
        val actions = message.actionRows.flatten()

        assertTrue(message.text.contains("Mini App для этого не нужен"))
        assertTrue(message.text.contains("Mini App ниже остаётся дополнительным интерфейсом"))
        assertEquals(listOf(2, 1), message.actionRows.map(List<*>::size))
        assertTrue(
            actions.any { action ->
                action is OutboundTelegramMessageAction.Callback &&
                    action.data == TelegramMenuScreen.OPEN_CALLBACK_DATA &&
                    action.style == OutboundTelegramButtonStyle.PRIMARY
            },
        )
        assertTrue(
            actions.any { action ->
                action is OutboundTelegramMessageAction.Callback &&
                    action.data == TelegramHelpSection.OVERVIEW.callbackData
            },
        )
        assertTrue(actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
    }

    @Test
    fun `start localizes persisted ukrainian language`() {
        val message = renderer.renderStart(startContext(), TelegramSettingsLanguage.UK)
        val labels = message.actionRows.flatten().map(OutboundTelegramMessageAction::label)

        assertTrue(message.text.contains("Ласкаво просимо до Mnemos"))
        assertTrue(message.text.contains("Mini App для цього не потрібен"))
        assertTrue(labels.any { label -> label.contains("Допомога") })
    }

    @Test
    fun `help localizes persisted english language`() {
        val message = renderer.renderHelp(TelegramHelpSection.SUBSCRIPTIONS, TelegramSettingsLanguage.EN)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val activeSection = callbacks.first { action -> action.data == TelegramHelpSection.SUBSCRIPTIONS.callbackData }

        assertTrue(message.text.contains("Subscription controls match the Mini App"))
        assertTrue(message.text.contains("RAID is edited as one subscription"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, activeSection.style)
        assertTrue(activeSection.label.contains("Subscriptions"))
    }

    @Test
    fun `every help section keeps semantic help groups and optional mini app`() {
        TelegramHelpSection.entries.forEach { section ->
            val message = renderer.renderHelp(section, TelegramSettingsLanguage.RU)
            val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

            assertTrue(message.text.contains("<b>"))
            assertEquals(listOf(1, 2, 3, 2, 1), message.actionRows.map(List<*>::size))
            assertEquals(TelegramHelpSection.entries.size + 1, callbacks.size)
            assertTrue(
                callbacks.any { action ->
                    action.data == TelegramMenuScreen.OPEN_CALLBACK_DATA
                },
            )
            assertTrue(callbacks.any { action -> action.data == section.callbackData })
            assertTrue(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
        }
    }

    private fun startContext(): TelegramStartContext =
        TelegramStartContext(
            telegramId = 123L,
            firstName = "Knalis",
            username = "knalis",
        )
}
