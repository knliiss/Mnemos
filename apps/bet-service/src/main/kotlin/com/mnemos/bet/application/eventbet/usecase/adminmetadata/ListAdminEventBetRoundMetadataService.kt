package com.mnemos.bet.application.eventbet.usecase.adminmetadata

import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import com.mnemos.bet.application.eventbet.model.AdminEventBetRoundBettorView
import com.mnemos.bet.application.eventbet.model.AdminEventBetRoundMetadataView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAdminEventBetRoundMetadataService(
    private val reader: EventBetReader,
    private val accountSummaryProvider: AccountSummaryProvider,
) : ListAdminEventBetRoundMetadataUseCase {
    override fun execute(): List<AdminEventBetRoundMetadataView> {
        val rounds = reader.findAllRounds()

        if (rounds.isEmpty()) {
            return emptyList()
        }

        val bets = reader.findRoundBets(rounds.map { round -> round.id })
        val accountIds = bets.mapTo(linkedSetOf()) { bet -> bet.accountId }
        val accountSummaries =
            if (accountIds.isEmpty()) {
                emptyMap()
            } else {
                accountSummaryProvider.getByIds(accountIds)
            }
        val betsByRoundId = bets.groupBy { bet -> bet.roundId }

        return rounds.map { round ->
            val roundBets = betsByRoundId[round.id].orEmpty()
            val bettors =
                roundBets
                    .distinctBy { bet -> bet.accountId }
                    .map { bet ->
                        AdminEventBetRoundBettorView(
                            accountId = bet.accountId,
                            accountNickname = accountSummaries[bet.accountId]?.nickname,
                        )
                    }

            AdminEventBetRoundMetadataView(
                roundId = round.id,
                betCount = roundBets.size,
                bettors = bettors,
            )
        }
    }
}
