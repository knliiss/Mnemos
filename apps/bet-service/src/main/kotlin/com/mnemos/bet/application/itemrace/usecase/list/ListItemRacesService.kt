package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListItemRacesService(
    private val reader: ItemRaceReader,
    private val accountProvider: AccountProvider,
) : ListItemRacesUseCase {
    override fun execute(): List<GetItemRaceResult> {
        val account = accountProvider.requireActiveAccount()

        return reader.findCurrent().map { race ->
            val participant =
                reader.findParticipant(
                    raceId = race.id,
                    accountId = account.id,
                )

            GetItemRaceResult(
                race = race,
                participants = listOfNotNull(participant),
            )
        }
    }
}
