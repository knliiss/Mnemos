package com.mnemos.bet.application.eventbet.usecase.get

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.common.core.model.permission.RolePermission.VIEW_BETS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetEventBetRoundService(
    private val reader: EventBetReader,
    private val accountProvider: AccountProvider,
) : GetEventBetRoundUseCase {
    override fun execute(query: GetEventBetRoundQuery): GetEventBetRoundResult {
        val account = accountProvider.requireActiveAccount(VIEW_BETS)
        val round =
            reader.findRound(query.roundId)
                ?: throw BetNotFoundException("Event bet round ${query.roundId} was not found")

        return GetEventBetRoundResult(
            round = round,
            myBet = reader.findBet(round.id, account.id),
        )
    }
}
