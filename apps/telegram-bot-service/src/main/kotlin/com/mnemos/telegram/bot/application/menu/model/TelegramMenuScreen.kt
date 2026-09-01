package com.mnemos.telegram.bot.application.menu.model

enum class TelegramMenuScreen(
    val callbackData: String,
) {
    HOME("m:home"),
    MORE("m:more"),
    NOTIFICATIONS("m:notifications"),
    ONLINE("m:online"),
    EVENTS("m:events"),
    DAILY("m:daily"),
    ACCOUNT("m:account"),
    BETS("m:bets"),
    PRIME("m:prime"),
    SETTINGS("m:settings"),
    ;

    companion object {
        const val OPEN_CALLBACK_DATA = "m:open"
        const val BACK_CALLBACK_DATA = "m:back"

        fun fromCallbackData(callbackData: String): TelegramMenuScreen? =
            entries.firstOrNull { screen -> screen.callbackData == callbackData }
    }
}
