package com.mnemos.telegram.bot.application.widget.profiles.port.out

fun interface ProfileReactionQueryGuard {
    fun claim(telegramId: Long, queryId: String): Boolean
}
