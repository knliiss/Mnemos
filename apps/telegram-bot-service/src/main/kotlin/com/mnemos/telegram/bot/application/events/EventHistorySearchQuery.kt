package com.mnemos.telegram.bot.application.events

object EventHistorySearchQuery {
    const val INVALID_MESSAGE = "Введите от 1 до 64 символов."

    fun normalize(value: String): String? =
        value
            .trim()
            .takeIf { query -> query.length in MIN_LENGTH..MAX_LENGTH }

    private const val MIN_LENGTH = 1
    private const val MAX_LENGTH = 64
}
