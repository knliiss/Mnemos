package com.mnemos.moderation.domain.leaderboard.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class LeaderboardExclusionId(
    val id: UUID,
) {
    companion object {
        fun generate(): LeaderboardExclusionId = LeaderboardExclusionId(UuidCreator.getTimeOrderedEpoch())
    }
}
