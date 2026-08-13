package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.adapter.out.persistence.itemrace.ItemRacePersistenceMapper.toDomain
import com.mnemos.bet.adapter.out.persistence.itemrace.ItemRacePersistenceMapper.toEntity
import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class ItemRaceRepositoryAdapter(
    private val raceJpaRepository: ItemRaceJpaRepository,
    private val participantJpaRepository: ItemRaceParticipantJpaRepository,
) : ItemRaceRepository {
    override fun create(race: ItemRace): ItemRace = raceJpaRepository.save(race.toEntity()).toDomain()

    override fun save(race: ItemRace): ItemRace = raceJpaRepository.save(race.toEntity()).toDomain()

    override fun findByIdForUpdate(raceId: ItemRaceId): ItemRace? = raceJpaRepository.findByIdForUpdate(raceId.id)?.toDomain()

    override fun findRacesRequiringSynchronization(now: Instant): List<ItemRace> =
        raceJpaRepository
            .findRequiringSynchronization(
                now = now,
                synchronizingStatuses =
                    listOf(
                        ItemRaceStatus.REGISTRATION,
                        ItemRaceStatus.ACTIVE,
                        ItemRaceStatus.REFUNDING,
                    ),
                refundingStatus = ItemRaceStatus.REFUNDING,
            ).map { it.toDomain() }

    override fun createParticipant(participant: ItemRaceParticipant): ItemRaceParticipant =
        participantJpaRepository.save(participant.toEntity()).toDomain()

    override fun saveParticipant(participant: ItemRaceParticipant): ItemRaceParticipant =
        participantJpaRepository.save(participant.toEntity()).toDomain()

    override fun findParticipantByIdForUpdate(participantId: ItemRaceParticipantId): ItemRaceParticipant? =
        participantJpaRepository.findByIdForUpdate(participantId.id)?.toDomain()

    override fun findParticipantByRaceAndAccountForUpdate(
        raceId: ItemRaceId,
        accountId: AccountId,
    ): ItemRaceParticipant? = participantJpaRepository.findByRaceAndAccountForUpdate(raceId.id, accountId.id)?.toDomain()

    override fun findParticipantsByRaceForUpdate(raceId: ItemRaceId): List<ItemRaceParticipant> =
        participantJpaRepository.findByRaceForUpdate(raceId.id).map { it.toDomain() }
}
