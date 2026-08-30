package com.mnemos.telegram.bot.application.menu.model

enum class TelegramMenuScreen(
    val callbackData: String,
) {
    HOME("m:home"),
    NOTIFICATIONS("m:notifications"),
    ONLINE("m:online"),
    EVENTS("m:events"),
    DAILY("m:daily"),
    ACCOUNT("m:account"),
    SETTINGS("m:settings"),
    ;

    companion object {
        fun fromCallbackData(callbackData: String): TelegramMenuScreen? =
            entries.firstOrNull { screen -> screen.callbackData == callbackData }
    }
}
