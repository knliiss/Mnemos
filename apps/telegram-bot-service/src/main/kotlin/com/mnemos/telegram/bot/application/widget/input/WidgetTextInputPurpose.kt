package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen

enum class WidgetTextInputPurpose(val screen: TelegramWidgetScreen) {
    EVENT_SEARCH(TelegramWidgetScreen.EVENTS),
    ONLINE_PLAYER_SEARCH(TelegramWidgetScreen.ONLINE),
    ACCOUNT_CREATE(TelegramWidgetScreen.ACCOUNTS),
    ACCOUNT_JOIN(TelegramWidgetScreen.ACCOUNTS),
}
