package com.mnemos.bet.application.eventbet.usecase.adminlist

import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import com.mnemos.bet.application.eventbet.usecase.list.ListEventBetRoundResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAdminEventBetRoundsService(
    private val reader: EventBetReader,
) : ListAdminEventBetRoundsUseCase {
    override fun execute(): List<ListEventBetRoundResult> =
        reader.findAllRounds().map { round ->
            ListEventBetRoundResult(
                round = round,
                myBet = null,
            )
        }
}
