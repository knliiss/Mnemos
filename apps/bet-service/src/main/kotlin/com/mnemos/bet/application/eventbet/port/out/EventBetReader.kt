package com.mnemos.bet.application.eventbet.port.out

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.common.core.model.id.AccountId

interface EventBetReader {
    fun findCurrentRounds(): List<EventBetRoundView>

    fun findAllRounds(): List<EventBetRoundView>

    fun findRound(roundId: EventBetRoundId): EventBetRoundView?

    fun findBet(
        roundId: EventBetRoundId,
        accountId: AccountId,
    ): EventBetView?

    fun findBets(
        roundIds: Collection<EventBetRoundId>,
        accountId: AccountId,
    ): List<EventBetView>

    fun findRoundBets(roundId: EventBetRoundId): List<EventBetView>

    fun findRoundBets(roundIds: Collection<EventBetRoundId>): List<EventBetView> {
        if (roundIds.isEmpty()) {
            return emptyList()
        }

        return roundIds.flatMap { roundId -> findRoundBets(roundId) }
    }

    fun findHistory(accountId: AccountId): List<EventBetView>
}
