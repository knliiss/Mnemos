package com.mnemos.bet.application.eventbet.usecase.history

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.eventbet.model.EventBetView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListMyEventBetsService(
    private val reader: EventBetReader,
    private val accountProvider: AccountProvider,
) : ListMyEventBetsUseCase {
    override fun execute(): List<EventBetView> = reader.findHistory(accountProvider.requireActiveAccount().id)
}
