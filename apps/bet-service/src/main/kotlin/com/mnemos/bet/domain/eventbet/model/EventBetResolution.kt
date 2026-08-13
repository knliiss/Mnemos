package com.mnemos.bet.domain.eventbet.model

import com.mnemos.common.core.model.id.UserId
import java.time.Instant

class EventBetResolution private constructor(
    val id: EventBetResolutionId,
    val roundId: EventBetRoundId,
    val outcome: EventBetOutcome,
    val source: EventBetResolutionSource,
    val resolvedByUserId: UserId?,
    val reason: String?,
    val resolvedAt: Instant,
) {
    companion object {
        fun automatic(
            roundId: EventBetRoundId,
            outcome: EventBetOutcome,
            resolvedAt: Instant,
        ): EventBetResolution =
            EventBetResolution(
                id = EventBetResolutionId.generate(),
                roundId = roundId,
                outcome = outcome,
                source = EventBetResolutionSource.AUTOMATIC,
                resolvedByUserId = null,
                reason = null,
                resolvedAt = resolvedAt,
            )

        fun administrative(
            roundId: EventBetRoundId,
            outcome: EventBetOutcome,
            resolvedByUserId: UserId,
            reason: String,
            resolvedAt: Instant,
        ): EventBetResolution {
            require(reason.isNotBlank())

            return EventBetResolution(
                id = EventBetResolutionId.generate(),
                roundId = roundId,
                outcome = outcome,
                source = EventBetResolutionSource.ADMINISTRATIVE,
                resolvedByUserId = resolvedByUserId,
                reason = reason,
                resolvedAt = resolvedAt,
            )
        }

        fun restore(
            id: EventBetResolutionId,
            roundId: EventBetRoundId,
            outcome: EventBetOutcome,
            source: EventBetResolutionSource,
            resolvedByUserId: UserId?,
            reason: String?,
            resolvedAt: Instant,
        ): EventBetResolution =
            EventBetResolution(
                id = id,
                roundId = roundId,
                outcome = outcome,
                source = source,
                resolvedByUserId = resolvedByUserId,
                reason = reason,
                resolvedAt = resolvedAt,
            )
    }
}
