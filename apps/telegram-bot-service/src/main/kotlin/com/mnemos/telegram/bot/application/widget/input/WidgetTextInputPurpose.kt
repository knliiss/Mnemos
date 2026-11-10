package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen

enum class WidgetTextInputPurpose(val screen: TelegramWidgetScreen) {
    EVENT_SEARCH(TelegramWidgetScreen.EVENTS),
    ONLINE_PLAYER_SEARCH(TelegramWidgetScreen.ONLINE),
    ACCOUNT_CREATE(TelegramWidgetScreen.ACCOUNTS),
    ACCOUNT_JOIN(TelegramWidgetScreen.ACCOUNTS),
    ITEM_DROP_PLAYER(TelegramWidgetScreen.NOTIFICATIONS),
    ITEM_DROP_ITEM(TelegramWidgetScreen.NOTIFICATIONS),
    ITEM_DROP_BOTH(TelegramWidgetScreen.NOTIFICATIONS),
    PUBLIC_PROFILE_SEARCH(TelegramWidgetScreen.PROFILES),
    ACCOUNT_ROLE_CREATE(TelegramWidgetScreen.ACCOUNT_MANAGEMENT),
}
