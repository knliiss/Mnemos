package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.adapter.out.persistence.eventbet.EventBetPersistenceMapper.toDomain
import com.mnemos.bet.adapter.out.persistence.eventbet.EventBetPersistenceMapper.toEntity
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class EventBetRepositoryAdapter(
    private val roundJpaRepository: EventBetRoundJpaRepository,
    private val betJpaRepository: EventBetJpaRepository,
    private val resolutionJpaRepository: EventBetResolutionJpaRepository,
) : EventBetRepository {
    override fun createRound(round: EventBetRound): EventBetRound = roundJpaRepository.save(round.toEntity()).toDomain()

    override fun saveRound(round: EventBetRound): EventBetRound = roundJpaRepository.save(round.toEntity()).toDomain()

    override fun findRoundByIdForUpdate(roundId: EventBetRoundId): EventBetRound? =
        roundJpaRepository.findByIdForUpdate(roundId.id)?.toDomain()

    override fun findRoundByEventAndScheduleForUpdate(
        eventType: DomainBetGlobalEventType,
        scheduledAt: Instant,
    ): EventBetRound? =
        roundJpaRepository
            .findByEventAndScheduleForUpdate(
                eventType = eventType,
                scheduledAt = scheduledAt,
            )?.toDomain()

    override fun findRoundsRequiringSynchronization(now: Instant): List<EventBetRound> =
        roundJpaRepository
            .findRequiringSynchronization(
                now = now,
                settledStatus = EventBetRoundStatus.SETTLED,
            ).map { it.toDomain() }

    override fun createBet(bet: EventBet): EventBet = betJpaRepository.save(bet.toEntity()).toDomain()

    override fun saveBet(bet: EventBet): EventBet = betJpaRepository.save(bet.toEntity()).toDomain()

    override fun findBetByIdForUpdate(betId: EventBetId): EventBet? = betJpaRepository.findByIdForUpdate(betId.id)?.toDomain()

    override fun findBetByRoundAndAccountForUpdate(
        roundId: EventBetRoundId,
        accountId: AccountId,
    ): EventBet? = betJpaRepository.findByRoundAndAccountForUpdate(roundId.id, accountId.id)?.toDomain()

    override fun findBetsByRoundForUpdate(roundId: EventBetRoundId): List<EventBet> =
        betJpaRepository.findByRoundForUpdate(roundId.id).map { it.toDomain() }

    override fun createResolution(resolution: EventBetResolution): EventBetResolution =
        resolutionJpaRepository.save(resolution.toEntity()).toDomain()
}
