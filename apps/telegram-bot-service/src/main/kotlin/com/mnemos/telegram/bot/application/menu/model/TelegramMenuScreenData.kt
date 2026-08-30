package com.mnemos.telegram.bot.application.menu.model

import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData

sealed interface TelegramMenuScreenData {
    data object Empty : TelegramMenuScreenData

    data class Notifications(
        val data: NotificationMenuScreenData,
    ) : TelegramMenuScreenData

    data class SaoOnline(
        val data: SaoOnlineMenuScreenData,
    ) : TelegramMenuScreenData

    data class EventHistory(
        val data: EventHistoryMenuScreenData,
    ) : TelegramMenuScreenData
}
