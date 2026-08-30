package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import org.springframework.stereotype.Component

@Component
class SettingsMenuScreenDataProvider(
    private val settingsGateway: SettingsGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.SETTINGS

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.Settings(
            snapshot = settingsGateway.getSettings(session.telegramId),
        )
}
