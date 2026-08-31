package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import java.util.UUID

object BetMenuContext {
    private const val MODE_KEY = "bet.mode"
    private const val ROUND_ID_KEY = "bet.roundId"
    private const val RACE_ID_KEY = "bet.raceId"
    private const val SIDE_KEY = "bet.side"
    private const val IDEMPOTENCY_SEED_KEY = "bet.idempotencySeed"
    private const val FEEDBACK_KEY = "bet.feedback"
    private const val INCREASE_KEY = "bet.increase"

    fun mode(context: Map<String, String>): BetMenuMode =
        context[MODE_KEY]
            ?.let { value -> enumValueOrNull<BetMenuMode>(value) }
            ?: BetMenuMode.ROUNDS

    fun roundId(context: Map<String, String>): UUID? = context[ROUND_ID_KEY]?.let(::uuidOrNull)

    fun raceId(context: Map<String, String>): UUID? = context[RACE_ID_KEY]?.let(::uuidOrNull)

    fun side(context: Map<String, String>): TelegramBetSide? =
        context[SIDE_KEY]?.let { value -> enumValueOrNull<TelegramBetSide>(value) }

    fun idempotencySeed(context: Map<String, String>): String? = context[IDEMPOTENCY_SEED_KEY]

    fun feedback(context: Map<String, String>): BetMenuFeedback? =
        context[FEEDBACK_KEY]?.let { value -> enumValueOrNull<BetMenuFeedback>(value) }

    fun increasing(context: Map<String, String>): Boolean = context[INCREASE_KEY] == "true"

    fun rounds(): Map<String, String> = mapOf(MODE_KEY to BetMenuMode.ROUNDS.name)

    fun races(): Map<String, String> = mapOf(MODE_KEY to BetMenuMode.RACES.name)

    fun history(): Map<String, String> = mapOf(MODE_KEY to BetMenuMode.HISTORY.name)

    fun requestPlace(
        roundId: UUID,
        side: TelegramBetSide,
        idempotencySeed: String,
    ): Map<String, String> =
        mapOf(
            MODE_KEY to BetMenuMode.ROUNDS.name,
            ROUND_ID_KEY to roundId.toString(),
            SIDE_KEY to side.name,
            IDEMPOTENCY_SEED_KEY to idempotencySeed,
            INCREASE_KEY to "false",
        )

    fun requestIncrease(
        roundId: UUID,
        idempotencySeed: String,
    ): Map<String, String> =
        mapOf(
            MODE_KEY to BetMenuMode.ROUNDS.name,
            ROUND_ID_KEY to roundId.toString(),
            IDEMPOTENCY_SEED_KEY to idempotencySeed,
            INCREASE_KEY to "true",
        )

    fun requestRaceIncrease(
        raceId: UUID,
        idempotencySeed: String,
    ): Map<String, String> =
        mapOf(
            MODE_KEY to BetMenuMode.RACES.name,
            RACE_ID_KEY to raceId.toString(),
            IDEMPOTENCY_SEED_KEY to idempotencySeed,
            INCREASE_KEY to "true",
        )

    fun withFeedback(
        current: Map<String, String>,
        feedback: BetMenuFeedback,
    ): Map<String, String> = current + (FEEDBACK_KEY to feedback.name)

    fun accepted(mode: BetMenuMode = BetMenuMode.ROUNDS): Map<String, String> =
        mapOf(
            MODE_KEY to mode.name,
            FEEDBACK_KEY to BetMenuFeedback.ACCEPTED.name,
        )

    fun accountRequired(mode: BetMenuMode = BetMenuMode.ROUNDS): Map<String, String> =
        mapOf(
            MODE_KEY to mode.name,
            FEEDBACK_KEY to BetMenuFeedback.ACCOUNT_REQUIRED.name,
        )

    private fun uuidOrNull(value: String): UUID? = runCatching { UUID.fromString(value) }.getOrNull()

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == value }
}
