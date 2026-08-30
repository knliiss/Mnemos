package com.mnemos.telegram.bot.application.menu.model

import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData

sealed interface TelegramMenuScreenData {
    data object Empty : TelegramMenuScreenData

    data class SaoOnline(
        val data: SaoOnlineMenuScreenData,
    ) : TelegramMenuScreenData
}
