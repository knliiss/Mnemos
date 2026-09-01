package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.settings.model.TelegramNotificationPackOption
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class SettingsMenuMessageRendererTest {
    private val renderer = LocalTelegramMenuMessageRenderer()

    @Test
    fun `settings render structured language and active notification pack`() {
        val packId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val data =
            TelegramMenuScreenData.Settings(
                snapshot =
                    TelegramSettingsSnapshot(
                        localizationLanguage = TelegramSettingsLanguage.UK,
                        activeNotificationPackId = packId,
                        activeNotificationPackTitle = "Night <Ops>",
                        notificationPacks =
                            listOf(
                                TelegramNotificationPackOption(
                                    personalizationId = packId,
                                    title = "Night <Ops>",
                                    active = true,
                                ),
                            ),
                    ),
                mutationResult = null,
            )

        val message = renderer.render(TelegramMenuScreen.SETTINGS, data, TelegramSettingsLanguage.RU)
        val callbacks = message.actionRows.flatten().filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val activePack = callbacks.first { action -> action.label.startsWith("🔔 Night") }

        assertTrue(message.text.contains("🌐 <b>Язык</b>"))
        assertTrue(message.text.contains("└ 🇺🇦 Українська"))
        assertTrue(message.text.contains("🔔 <b>Пак уведомлений</b>"))
        assertTrue(message.text.contains("└ Night &lt;Ops&gt;"))
        assertEquals(3, message.actionRows.first().size)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, activePack.style)
        assertFalse(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
    }

    @Test
    fun `settings render missing pack and successful mutation feedback`() {
        val data =
            TelegramMenuScreenData.Settings(
                snapshot =
                    TelegramSettingsSnapshot(
                        localizationLanguage = TelegramSettingsLanguage.RU,
                        activeNotificationPackId = null,
                        activeNotificationPackTitle = null,
                        notificationPacks = emptyList(),
                    ),
                mutationResult = TelegramSettingsMutationResult.UPDATED,
            )

        val message = renderer.render(TelegramMenuScreen.SETTINGS, data, TelegramSettingsLanguage.RU)

        assertTrue(message.text.contains("🔔 <b>Пак уведомлений</b>"))
        assertTrue(message.text.contains("└ Не выбран"))
        assertTrue(message.text.contains("✅ Настройка сохранена."))
        assertTrue(message.text.contains("Паки уведомлений пока недоступны."))
        assertEquals(listOf(3, 1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `settings render recoverable unavailable state`() {
        val data =
            TelegramMenuScreenData.Settings(
                snapshot = null,
                mutationResult = null,
            )

        val message = renderer.render(TelegramMenuScreen.SETTINGS, data, TelegramSettingsLanguage.RU)

        assertTrue(message.text.contains("Не удалось загрузить персональные настройки"))
        assertEquals(listOf(1, 1), message.actionRows.map(List<*>::size))
    }
}
