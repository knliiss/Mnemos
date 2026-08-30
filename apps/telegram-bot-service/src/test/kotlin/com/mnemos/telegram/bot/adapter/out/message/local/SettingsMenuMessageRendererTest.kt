package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsMenuMessageRendererTest {
    private val renderer = LocalTelegramMenuMessageRenderer()

    @Test
    fun `settings render active notification pack and escape html`() {
        val data =
            TelegramMenuScreenData.Settings(
                TelegramSettingsSnapshot(
                    activeNotificationPackTitle = "Night <Ops>",
                ),
            )

        val message = renderer.render(TelegramMenuScreen.SETTINGS, data)

        assertTrue(message.text.contains("Night &lt;Ops&gt;"))
        assertTrue(message.actions.any { action -> action is OutboundTelegramMessageAction.OpenMiniApp })
    }

    @Test
    fun `settings render missing pack as not selected`() {
        val data =
            TelegramMenuScreenData.Settings(
                TelegramSettingsSnapshot(
                    activeNotificationPackTitle = null,
                ),
            )

        val message = renderer.render(TelegramMenuScreen.SETTINGS, data)

        assertTrue(message.text.contains("Пак уведомлений: <b>Не выбран</b>"))
    }

    @Test
    fun `settings render recoverable unavailable state`() {
        val data = TelegramMenuScreenData.Settings(snapshot = null)

        val message = renderer.render(TelegramMenuScreen.SETTINGS, data)

        assertTrue(message.text.contains("Не удалось загрузить персональные настройки"))
    }
}
