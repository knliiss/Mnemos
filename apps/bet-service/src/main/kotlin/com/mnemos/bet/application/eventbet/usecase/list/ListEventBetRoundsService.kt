package com.mnemos.bet.application.eventbet.usecase.list

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListEventBetRoundsService(
    private val reader: EventBetReader,
    private val accountProvider: AccountProvider,
) : ListEventBetRoundsUseCase {
    override fun execute(): List<EventBetRoundView> {
        accountProvider.requireActiveAccount()

        return reader.findCurrentRounds()
    }
}
