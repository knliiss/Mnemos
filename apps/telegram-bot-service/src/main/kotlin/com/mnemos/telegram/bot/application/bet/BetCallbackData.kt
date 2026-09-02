package com.mnemos.telegram.bot.application.bet

import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import java.util.UUID

object BetCallbackData {
    const val PREFIX = "bet:"
    const val REFRESH = "bet:refresh"
    const val HISTORY = "bet:history"
    const val ROUNDS = "bet:rounds"
    const val RACES = "bet:races"
    const val CANCEL_INPUT = "bet:cancel"

    fun place(
        roundId: UUID,
        side: TelegramBetSide,
    ): String = "bet:place:$roundId:${side.name}"

    fun increase(roundId: UUID): String = "bet:increase:$roundId"

    fun joinRace(raceId: UUID): String = "bet:race:join:$raceId"

    fun increaseRace(raceId: UUID): String = "bet:race:increase:$raceId"

    fun placeSelection(data: String): Pair<UUID, TelegramBetSide>? {
        val parts = data.split(':')

        if (parts.size != 4 || parts[0] != "bet" || parts[1] != "place") {
            return null
        }

        val roundId = runCatching { UUID.fromString(parts[2]) }.getOrNull() ?: return null
        val side = runCatching { TelegramBetSide.valueOf(parts[3]) }.getOrNull() ?: return null

        return roundId to side
    }

    fun increasedRoundId(data: String): UUID? = idFromThreePartCallback(data, "increase")

    fun joinedRaceId(data: String): UUID? = idFromFourPartCallback(data, "race", "join")

    fun increasedRaceId(data: String): UUID? = idFromFourPartCallback(data, "race", "increase")

    private fun idFromThreePartCallback(
        data: String,
        action: String,
    ): UUID? {
        val parts = data.split(':')

        if (parts.size != 3 || parts[0] != "bet" || parts[1] != action) {
            return null
        }

        return runCatching { UUID.fromString(parts[2]) }.getOrNull()
    }

    private fun idFromFourPartCallback(
        data: String,
        scope: String,
        action: String,
    ): UUID? {
        val parts = data.split(':')

        if (parts.size != 4 || parts[0] != "bet" || parts[1] != scope || parts[2] != action) {
            return null
        }

        return runCatching { UUID.fromString(parts[3]) }.getOrNull()
    }
}
