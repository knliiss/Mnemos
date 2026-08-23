package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.adapter.out.persistence.eventbet.EventBetPersistenceMapper.toView
import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository

@Repository
class EventBetReaderAdapter(
    private val roundJpaRepository: EventBetRoundJpaRepository,
    private val betJpaRepository: EventBetJpaRepository,
) : EventBetReader {
    override fun findCurrentRounds(): List<EventBetRoundView> =
        roundJpaRepository
            .findCurrent(
                settledStatus = EventBetRoundStatus.SETTLED,
            ).map { it.toView() }

    override fun findRound(roundId: EventBetRoundId): EventBetRoundView? {
        return roundJpaRepository.findById(roundId.id).orElse(null)?.toView()
    }

    override fun findBet(
        roundId: EventBetRoundId,
        accountId: AccountId,
    ): EventBetView? = betJpaRepository.findByRoundIdAndAccountId(roundId.id, accountId.id)?.toView()

    override fun findBets(
        roundIds: Collection<EventBetRoundId>,
        accountId: AccountId,
    ): List<EventBetView> {
        if (roundIds.isEmpty()) {
            return emptyList()
        }

        return betJpaRepository
            .findAllByRoundIdInAndAccountId(
                roundIds = roundIds.map { it.id },
                accountId = accountId.id,
            ).map { it.toView() }
    }

    override fun findHistory(accountId: AccountId): List<EventBetView> =
        betJpaRepository.findAllByAccountIdOrderByIdDesc(accountId.id).map { it.toView() }
}
