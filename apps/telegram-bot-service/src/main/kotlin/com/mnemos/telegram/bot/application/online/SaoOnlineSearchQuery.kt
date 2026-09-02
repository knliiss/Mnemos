package com.mnemos.telegram.bot.application.online

object SaoOnlineSearchQuery {
    const val INVALID_MESSAGE = "Введите от 2 до 32 символов без пробелов."

    fun normalize(value: String): String? {
        val query = value.trim()

        if (query.length !in MIN_LENGTH..MAX_LENGTH) {
            return null
        }

        if (query.any { character -> character.isWhitespace() }) {
            return null
        }

        return query
    }

    private const val MIN_LENGTH = 2
    private const val MAX_LENGTH = 32
}
