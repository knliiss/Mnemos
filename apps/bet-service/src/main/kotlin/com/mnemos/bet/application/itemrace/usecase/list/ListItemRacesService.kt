package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
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
        val ongoingParticipants = reader.findOngoingParticipants(account.id)
        val participantByRaceId = ongoingParticipants.associateBy { it.raceId }
        val raceById = LinkedHashMap<ItemRaceId, ItemRaceView>()

        reader.findCurrent().forEach { race ->
            raceById[race.id] = race
        }

        ongoingParticipants.forEach { participant ->
            if (raceById.containsKey(participant.raceId)) {
                return@forEach
            }

            val race = reader.find(participant.raceId) ?: return@forEach

            raceById[race.id] = race
        }

        return raceById
            .values
            .sortedWith(compareBy<ItemRaceView> { it.startsAt }.thenBy { it.id.id })
            .map { race ->
                GetItemRaceResult(
                    race = race,
                    participants = listOfNotNull(participantByRaceId[race.id]),
                )
            }
    }
}
