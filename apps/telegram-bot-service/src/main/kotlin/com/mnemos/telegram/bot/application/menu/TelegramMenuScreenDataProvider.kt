package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession

interface TelegramMenuScreenDataProvider {
    val screen: TelegramMenuScreen

    fun load(session: TelegramMenuSession): TelegramMenuScreenData
}
