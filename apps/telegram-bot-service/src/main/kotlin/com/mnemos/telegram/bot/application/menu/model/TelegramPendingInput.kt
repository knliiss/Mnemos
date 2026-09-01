package com.mnemos.telegram.bot.application.menu.model

enum class TelegramPendingInput(
    val screen: TelegramMenuScreen,
) {
    EVENT_SEARCH(TelegramMenuScreen.EVENTS),
    ONLINE_PLAYER_SEARCH(TelegramMenuScreen.ONLINE),
    NOTIFICATION_ITEM_DROP_PLAYER(TelegramMenuScreen.NOTIFICATIONS),
    NOTIFICATION_ITEM_DROP_ITEM(TelegramMenuScreen.NOTIFICATIONS),
    ACCOUNT_CREATE(TelegramMenuScreen.ACCOUNT),
    ACCOUNT_JOIN(TelegramMenuScreen.ACCOUNT),
    BET_PLACE_AMOUNT(TelegramMenuScreen.BETS),
    BET_INCREASE_AMOUNT(TelegramMenuScreen.BETS),
    BET_RACE_INCREASE_AMOUNT(TelegramMenuScreen.BETS),
}