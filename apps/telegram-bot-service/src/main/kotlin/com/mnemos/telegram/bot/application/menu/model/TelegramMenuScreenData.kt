package com.mnemos.telegram.bot.application.menu.model

import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.daily.model.DailyMenuScreenData
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

    data class Daily(
        val data: DailyMenuScreenData,
    ) : TelegramMenuScreenData

    data class Account(
        val data: AccountMenuScreenData,
    ) : TelegramMenuScreenData
}
