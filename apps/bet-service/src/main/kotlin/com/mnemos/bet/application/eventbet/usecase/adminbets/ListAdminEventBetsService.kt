package com.mnemos.bet.application.eventbet.usecase.adminbets

import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import com.mnemos.bet.application.eventbet.model.AdminEventBetView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAdminEventBetsService(
    private val reader: EventBetReader,
    private val accountSummaryProvider: AccountSummaryProvider,
) : ListAdminEventBetsUseCase {
    override fun execute(roundId: EventBetRoundId): List<AdminEventBetView> {
        val bets = reader.findRoundBets(roundId)
        val summaries =
            accountSummaryProvider.getByIds(
                bets.mapTo(linkedSetOf()) { bet -> bet.accountId },
            )

        return bets.map { bet ->
            AdminEventBetView(
                id = bet.id,
                roundId = bet.roundId,
                accountId = bet.accountId,
                accountNickname = summaries[bet.accountId]?.nickname,
                side = bet.side,
                confirmedAmount = bet.confirmedAmount,
                pendingAmount = bet.pendingAmount,
                status = bet.status,
            )
        }
    }
}
