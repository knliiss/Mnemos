package com.mnemos.telegram.bot.application.command.help

enum class TelegramHelpSection(
    val callbackData: String,
) {
    OVERVIEW("help:overview"),
    MINI_APP("help:mini-app"),
    SUBSCRIPTIONS("help:subscriptions"),
    ACCOUNTS("help:accounts"),
    BETS("help:bets"),
    NOTIFICATIONS("help:notifications"),
    TROUBLESHOOTING("help:troubleshooting"),
    ;

    companion object {
        fun fromCallbackData(value: String): TelegramHelpSection? = entries.firstOrNull { section -> section.callbackData == value }
    }
}
