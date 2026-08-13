package com.mnemos.bet.application.itemrace.usecase.history

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListMyItemRaceHistoryService(
    private val reader: ItemRaceReader,
    private val accountProvider: AccountProvider,
) : ListMyItemRaceHistoryUseCase {
    override fun execute(): List<ItemRaceParticipantView> = reader.findHistory(accountProvider.requireActiveAccount().id)
}
