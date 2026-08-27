package com.mnemos.moderation.application.leaderboard.mapper

import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.LeaderboardExclusionChangedV1
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion
import java.time.Instant

object LeaderboardExclusionIntegrationEventMapper {
    fun LeaderboardExclusion.toIntegrationEvent(
        active: Boolean,
        occurredAt: Instant,
    ): IntegrationEvent<LeaderboardExclusionChangedV1> =
        IntegrationEvent(
            type = ModerationEventType.LEADERBOARD_EXCLUSION_CHANGED,
            aggregateId = id.id,
            occurredAt = occurredAt,
            payload =
                LeaderboardExclusionChangedV1(
                    exclusionId = id.id,
                    subjectType = subjectType,
                    subjectId = subjectId,
                    scope = scope,
                    active = active,
                    occurredAt = occurredAt,
                ),
        )
}
