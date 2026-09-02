package com.mnemos.telegram.bot.application.menu.model

import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuScreenData
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuScreenData
import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.daily.model.DailyMenuScreenData
import com.mnemos.telegram.bot.application.events.model.EventHistoryMenuScreenData
import com.mnemos.telegram.bot.application.notification.model.NotificationMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.prime.model.PrimeMenuScreenData
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot

sealed interface TelegramMenuScreenData {
    data object Empty : TelegramMenuScreenData

    data class Notifications(
        val data: NotificationMenuScreenData,
    ) : TelegramMenuScreenData

    data class Packs(
        val data: NotificationPackMenuScreenData,
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

    data class AccountManagement(
        val data: AccountManagementMenuScreenData,
    ) : TelegramMenuScreenData

    data class AccountInvites(
        val data: AccountInviteMenuScreenData,
    ) : TelegramMenuScreenData

    data class Bets(
        val data: BetMenuScreenData,
    ) : TelegramMenuScreenData

    data class Prime(
        val data: PrimeMenuScreenData,
    ) : TelegramMenuScreenData

    data class Settings(
        val snapshot: TelegramSettingsSnapshot?,
        val mutationResult: TelegramSettingsMutationResult?,
    ) : TelegramMenuScreenData
}
