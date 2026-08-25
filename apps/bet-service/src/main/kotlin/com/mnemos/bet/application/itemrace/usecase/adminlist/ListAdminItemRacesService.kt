package com.mnemos.bet.application.itemrace.usecase.adminlist

import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.application.itemrace.usecase.get.GetItemRaceResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListAdminItemRacesService(
    private val reader: ItemRaceReader,
) : ListAdminItemRacesUseCase {
    override fun execute(): List<GetItemRaceResult> {
        val races = reader.findAllForAdministration()
        val participantsByRaceId =
            reader
                .findParticipants(races.map { it.id })
                .groupBy { it.raceId }

        return races.map { race ->
            GetItemRaceResult(
                race = race,
                participants = participantsByRaceId[race.id].orEmpty(),
            )
        }
    }
}
