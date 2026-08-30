package com.mnemos.telegram.bot.application.menu.port.out

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage

interface TelegramMenuMessageRenderer {
    fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
    ): OutboundTelegramMessage
}
