package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalTelegramCommandMessageRendererTest {
    private val renderer = LocalTelegramCommandMessageRenderer()

    @Test
    fun `start prioritizes menu and keeps secondary actions separate`() {
        val message = renderer.renderStart(startContext(), TelegramSettingsLanguage.RU)
        val actions = message.actionRows.flatten()

        assertTrue(message.text.contains("Mini App для этого не нужен"))
        assertFalse(message.text.contains("Mini App ниже остаётся дополнительным интерфейсом"))
        assertEquals(listOf(1, 1, 1), message.actionRows.map(List<*>::size))
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
    fun `subscription help is self contained and localizes to english`() {
        val message = renderer.renderHelp(TelegramHelpSection.SUBSCRIPTIONS, TelegramSettingsLanguage.EN)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val activeSection = callbacks.first { action -> action.data == TelegramHelpSection.SUBSCRIPTIONS.callbackData }

        assertTrue(message.text.contains("Global events"))
        assertFalse(message.text.contains("match the Mini App"))
        assertFalse(message.text.contains("Mini App"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, activeSection.style)
        assertTrue(activeSection.label.contains("Subscriptions"))
    }

    @Test
    fun `overview help summarizes native bot capabilities`() {
        val message = renderer.renderHelp(TelegramHelpSection.OVERVIEW, TelegramSettingsLanguage.RU)

        assertTrue(message.text.contains("Основные команды"))
        assertTrue(message.text.contains("Cristalix-аккаунты"))
        assertTrue(message.text.contains("уведомления"))
        assertTrue(message.text.contains("Daily"))
        assertTrue(message.text.contains("PRIME"))
        assertFalse(message.text.contains("Нужен ли Mini App?"))
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
