package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.common.core.model.id.AccountId

object ItemRacePersistenceMapper {
    fun ItemRace.toEntity(): ItemRaceEntity =
        ItemRaceEntity(
            id = id.id,
            itemName = itemName,
            itemType = itemType,
            registrationClosesAt = registrationClosesAt,
            startsAt = startsAt,
            expiresAt = expiresAt,
            entryAmount = entryAmount,
            feePercent = feePercent,
            status = status,
            winnerAccountId = winnerAccountId?.id,
        )

    fun ItemRaceEntity.toDomain(): ItemRace =
        ItemRace.restore(
            id = ItemRaceId(id),
            itemName = itemName,
            itemType = itemType,
            registrationClosesAt = registrationClosesAt,
            startsAt = startsAt,
            expiresAt = expiresAt,
            entryAmount = entryAmount,
            feePercent = feePercent,
            status = status,
            winnerAccountId = winnerAccountId?.let(::AccountId),
        )

    fun ItemRaceParticipant.toEntity(): ItemRaceParticipantEntity =
        ItemRaceParticipantEntity(
            id = id.id,
            raceId = raceId.id,
            accountId = accountId.id,
            accountNickname = accountNickname,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )

    fun ItemRaceParticipantEntity.toDomain(): ItemRaceParticipant =
        ItemRaceParticipant.restore(
            id = ItemRaceParticipantId(id),
            raceId = ItemRaceId(raceId),
            accountId = AccountId(accountId),
            accountNickname = accountNickname,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
