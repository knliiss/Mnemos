package com.mnemos.telegram.bot.application.notification

object NotificationItemDropInputValidator {
    fun player(value: String): String? {
        val normalized = value.trim()

        if (normalized.length !in MIN_PLAYER_LENGTH..MAX_PLAYER_LENGTH) {
            return null
        }

        return normalized.takeIf(PLAYER_PATTERN::matches)
    }

    fun item(value: String): String? =
        value
            .trim()
            .takeIf(String::isNotEmpty)
            ?.takeIf { normalized -> normalized.length <= MAX_ITEM_LENGTH }

    private const val MIN_PLAYER_LENGTH = 4
    private const val MAX_PLAYER_LENGTH = 20
    private const val MAX_ITEM_LENGTH = 24

    private val PLAYER_PATTERN = Regex("^[\\p{L}0-9_]+$")
}
