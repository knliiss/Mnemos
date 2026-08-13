package com.mnemos.bet.domain.itemrace.repository

import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

interface ItemRaceRepository {
    fun create(race: ItemRace): ItemRace

    fun save(race: ItemRace): ItemRace

    fun findByIdForUpdate(raceId: ItemRaceId): ItemRace?

    fun findRacesRequiringSynchronization(now: Instant): List<ItemRace>

    fun createParticipant(participant: ItemRaceParticipant): ItemRaceParticipant

    fun saveParticipant(participant: ItemRaceParticipant): ItemRaceParticipant

    fun findParticipantByIdForUpdate(participantId: ItemRaceParticipantId): ItemRaceParticipant?

    fun findParticipantByRaceAndAccountForUpdate(
        raceId: ItemRaceId,
        accountId: AccountId,
    ): ItemRaceParticipant?

    fun findParticipantsByRaceForUpdate(raceId: ItemRaceId): List<ItemRaceParticipant>
}
