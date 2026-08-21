package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListItemRacesService(
    private val reader: ItemRaceReader,
    private val accountProvider: AccountProvider,
) : ListItemRacesUseCase {
    override fun execute(): List<ItemRaceView> {
        accountProvider.requireActiveAccount()

        return reader.findCurrent()
    }
}
