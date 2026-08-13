package com.mnemos.bet.adapter.out.persistence.eventbet

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.domain.eventbet.model.EventBetId
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

    override fun findRound(roundId: EventBetRoundId): EventBetRoundView? = roundJpaRepository.findById(roundId.id).orElse(null)?.toView()

    override fun findBet(
        roundId: EventBetRoundId,
        accountId: AccountId,
    ): EventBetView? = betJpaRepository.findByRoundIdAndAccountId(roundId.id, accountId.id)?.toView()

    override fun findHistory(accountId: AccountId): List<EventBetView> =
        betJpaRepository.findAllByAccountIdOrderByIdDesc(accountId.id).map { it.toView() }

    private fun EventBetRoundEntity.toView(): EventBetRoundView =
        EventBetRoundView(
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

    private fun EventBetEntity.toView(): EventBetView =
        EventBetView(
            id = EventBetId(id),
            roundId = EventBetRoundId(roundId),
            accountId = AccountId(accountId),
            side = side,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
