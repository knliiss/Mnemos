package com.mnemos.telegram.bot.application.identity.port.out

fun interface TelegramUserIdentityGateway {
    fun bootstrap(
        telegramId: Long,
        username: String?,
    ): Boolean
}
