package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetResolutionId
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

object EventBetPersistenceMapper {
    fun EventBetRound.toEntity(): EventBetRoundEntity =
        EventBetRoundEntity(
            id = id.id,
            eventType = eventType,
            scheduledAt = scheduledAt,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            yesOdds = yesOdds,
            noOdds = noOdds,
            status = status,
            effectiveOutcome = effectiveOutcome,
        )

    fun EventBetRoundEntity.toDomain(): EventBetRound =
        EventBetRound.restore(
            id = EventBetRoundId(id),
            eventType = eventType,
            scheduledAt = scheduledAt,
            closesAt = closesAt,
            resolvesAt = resolvesAt,
            yesOdds = yesOdds,
            noOdds = noOdds,
            status = status,
            effectiveOutcome = effectiveOutcome,
        )

    fun EventBet.toEntity(): EventBetEntity =
        EventBetEntity(
            id = id.id,
            roundId = roundId.id,
            accountId = accountId.id,
            side = side,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )

    fun EventBetEntity.toDomain(): EventBet =
        EventBet.restore(
            id = EventBetId(id),
            roundId = EventBetRoundId(roundId),
            accountId = AccountId(accountId),
            side = side,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )

    fun EventBetResolution.toEntity(): EventBetResolutionEntity =
        EventBetResolutionEntity(
            id = id.id,
            roundId = roundId.id,
            outcome = outcome,
            source = source,
            resolvedByUserId = resolvedByUserId?.id,
            reason = reason,
            resolvedAt = resolvedAt,
        )

    fun EventBetResolutionEntity.toDomain(): EventBetResolution =
        EventBetResolution.restore(
            id = EventBetResolutionId(id),
            roundId = EventBetRoundId(roundId),
            outcome = outcome,
            source = source,
            resolvedByUserId = resolvedByUserId?.let(::UserId),
            reason = reason,
            resolvedAt = resolvedAt,
        )
}
