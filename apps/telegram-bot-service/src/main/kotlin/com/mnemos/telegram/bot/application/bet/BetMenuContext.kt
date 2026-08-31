package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.TelegramBetOperationState
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import java.util.UUID

object BetMenuContext {
    private const val MODE_KEY = "bet.mode"
    private const val ROUND_ID_KEY = "bet.roundId"
    private const val RACE_ID_KEY = "bet.raceId"
    private const val SIDE_KEY = "bet.side"
    private const val IDEMPOTENCY_SEED_KEY = "bet.idempotencySeed"
    private const val FEEDBACK_KEY = "bet.feedback"
    private const val OPERATION_ID_KEY = "bet.operationId"
    private const val INCREASE_KEY = "bet.increase"

    fun mode(context: Map<String, String>): BetMenuMode =
        context[MODE_KEY]
            ?.let { value -> enumValueOrNull<BetMenuMode>(value) }
            ?: BetMenuMode.ROUNDS

    fun roundId(context: Map<String, String>): UUID? = context[ROUND_ID_KEY]?.let(::uuidOrNull)

    fun raceId(context: Map<String, String>): UUID? = context[RACE_ID_KEY]?.let(::uuidOrNull)

    fun operationId(context: Map<String, String>): UUID? = context[OPERATION_ID_KEY]?.let(::uuidOrNull)

    fun side(context: Map<String, String>): TelegramBetSide? =
        context[SIDE_KEY]?.let { value -> enumValueOrNull<TelegramBetSide>(value) }

    fun idempotencySeed(context: Map<String, String>): String? = context[IDEMPOTENCY_SEED_KEY]

    fun feedback(context: Map<String, String>): BetMenuFeedback? =
        context[FEEDBACK_KEY]?.let { value -> enumValueOrNull<BetMenuFeedback>(value) }

    fun increasing(context: Map<String, String>): Boolean = context[INCREASE_KEY] == "true"

    fun rounds(): Map<String, String> = mapOf(MODE_KEY to BetMenuMode.ROUNDS.name)

    fun races(): Map<String, String> = mapOf(MODE_KEY to BetMenuMode.RACES.name)

    fun history(): Map<String, String> = mapOf(MODE_KEY to BetMenuMode.HISTORY.name)

    fun refresh(
        current: Map<String, String>,
        mode: BetMenuMode,
    ): Map<String, String> =
        buildMap {
            put(MODE_KEY, mode.name)
            current[OPERATION_ID_KEY]?.let { operationId -> put(OPERATION_ID_KEY, operationId) }
            current[FEEDBACK_KEY]?.let { feedback -> put(FEEDBACK_KEY, feedback) }
        }

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

    fun operationResult(
        mode: BetMenuMode,
        operationId: UUID,
        state: TelegramBetOperationState,
    ): Map<String, String> =
        buildMap {
            put(MODE_KEY, mode.name)
            put(FEEDBACK_KEY, state.toFeedback().name)

            if (state == TelegramBetOperationState.PENDING) {
                put(OPERATION_ID_KEY, operationId.toString())
            }
        }

    fun accountRequired(mode: BetMenuMode = BetMenuMode.ROUNDS): Map<String, String> =
        mapOf(
            MODE_KEY to mode.name,
            FEEDBACK_KEY to BetMenuFeedback.ACCOUNT_REQUIRED.name,
        )

    fun feedbackForOperationState(state: TelegramBetOperationState): BetMenuFeedback = state.toFeedback()

    private fun TelegramBetOperationState.toFeedback(): BetMenuFeedback =
        when (this) {
            TelegramBetOperationState.COMPLETED -> BetMenuFeedback.COMPLETED
            TelegramBetOperationState.FAILED -> BetMenuFeedback.FAILED
            TelegramBetOperationState.PENDING -> BetMenuFeedback.PENDING
            TelegramBetOperationState.UNAVAILABLE -> BetMenuFeedback.UNAVAILABLE
        }

    private fun uuidOrNull(value: String): UUID? = runCatching { UUID.fromString(value) }.getOrNull()

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == value }
}
