package com.mnemos.bet.domain.eventbet.repository

import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBet
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

interface EventBetRepository {
    fun createRound(round: EventBetRound): EventBetRound

    fun saveRound(round: EventBetRound): EventBetRound

    fun findRoundByIdForUpdate(roundId: EventBetRoundId): EventBetRound?

    fun findRoundByEventAndScheduleForUpdate(
        eventType: DomainBetGlobalEventType,
        scheduledAt: Instant,
    ): EventBetRound?

    fun findRoundsRequiringSynchronization(now: Instant): List<EventBetRound>

    fun createBet(bet: EventBet): EventBet

    fun saveBet(bet: EventBet): EventBet

    fun findBetByIdForUpdate(betId: EventBetId): EventBet?

    fun findBetByRoundAndAccountForUpdate(
        roundId: EventBetRoundId,
        accountId: AccountId,
    ): EventBet?

    fun findBetsByRoundForUpdate(roundId: EventBetRoundId): List<EventBet>

    fun createResolution(resolution: EventBetResolution): EventBetResolution
}
