package com.mnemos.bet.application.eventbet.usecase.list

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListEventBetRoundsService(
    private val reader: EventBetReader,
    private val accountProvider: AccountProvider,
) : ListEventBetRoundsUseCase {
    override fun execute(): List<ListEventBetRoundResult> {
        val account = accountProvider.requireActiveAccount()

        return listForAccount(account.id)
    }

    fun executeFor(userId: UserId): List<ListEventBetRoundResult> {
        val account = accountProvider.requireActiveAccount(userId)

        return listForAccount(account.id)
    }

    private fun listForAccount(accountId: com.mnemos.common.core.model.id.AccountId): List<ListEventBetRoundResult> {
        val rounds = reader.findCurrentRounds()
        val betsByRoundId =
            reader
                .findBets(
                    roundIds = rounds.map { it.id },
                    accountId = accountId,
                ).associateBy { it.roundId }

        return rounds.map { round ->
            ListEventBetRoundResult(
                round = round,
                myBet = betsByRoundId[round.id],
            )
        }
    }
}
