package com.mnemos.telegram.bot.application.menu.port.out

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage

interface TelegramMenuMessageRenderer {
    fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage
}
