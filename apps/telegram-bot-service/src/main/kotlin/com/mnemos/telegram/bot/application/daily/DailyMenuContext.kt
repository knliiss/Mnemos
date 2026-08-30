package com.mnemos.telegram.bot.application.daily

import com.mnemos.telegram.bot.application.daily.model.DailyClaimedReward
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedback
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedbackType
import com.mnemos.telegram.bot.application.daily.model.DailyRestoredStreak

object DailyMenuContext {
    private const val FEEDBACK_KEY = "daily.feedback"
    private const val COINS_KEY = "daily.coins"
    private const val PRIMES_KEY = "daily.primes"
    private const val STREAK_KEY = "daily.streak"

    fun feedback(context: Map<String, String>): DailyMenuFeedback? {
        val type =
            context[FEEDBACK_KEY]
                ?.let { value -> enumValueOrNull<DailyMenuFeedbackType>(value) }
                ?: return null

        return DailyMenuFeedback(
            type = type,
            coins = context[COINS_KEY]?.toLongOrNull(),
            primes = context[PRIMES_KEY]?.toLongOrNull(),
            streak = context[STREAK_KEY]?.toIntOrNull(),
        )
    }

    fun claimed(reward: DailyClaimedReward): Map<String, String> =
        mapOf(
            FEEDBACK_KEY to DailyMenuFeedbackType.CLAIMED.name,
            COINS_KEY to reward.coins.toString(),
            PRIMES_KEY to reward.primes.toString(),
            STREAK_KEY to reward.streak.toString(),
        )

    fun restored(streak: DailyRestoredStreak): Map<String, String> =
        mapOf(
            FEEDBACK_KEY to DailyMenuFeedbackType.RESTORED.name,
            STREAK_KEY to streak.currentStreak.toString(),
        )

    fun unavailable(): Map<String, String> =
        mapOf(FEEDBACK_KEY to DailyMenuFeedbackType.UNAVAILABLE.name)

    fun clear(): Map<String, String> = emptyMap()

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == value }
}
