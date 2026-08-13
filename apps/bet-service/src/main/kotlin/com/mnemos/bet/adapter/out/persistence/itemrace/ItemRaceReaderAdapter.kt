package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantId
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository

@Repository
class ItemRaceReaderAdapter(
    private val raceJpaRepository: ItemRaceJpaRepository,
    private val participantJpaRepository: ItemRaceParticipantJpaRepository,
) : ItemRaceReader {
    override fun findCurrent(): List<ItemRaceView> =
        raceJpaRepository
            .findCurrent(
                currentStatuses =
                    listOf(
                        ItemRaceStatus.REGISTRATION,
                        ItemRaceStatus.ACTIVE,
                        ItemRaceStatus.SETTLING,
                    ),
            ).map { it.toView() }

    override fun find(raceId: ItemRaceId): ItemRaceView? = raceJpaRepository.findById(raceId.id).orElse(null)?.toView()

    override fun findParticipants(raceId: ItemRaceId): List<ItemRaceParticipantView> =
        participantJpaRepository.findAllByRaceIdOrderById(raceId.id).map { it.toView() }

    override fun findHistory(accountId: AccountId): List<ItemRaceParticipantView> =
        participantJpaRepository.findAllByAccountIdOrderByIdDesc(accountId.id).map { it.toView() }

    private fun ItemRaceEntity.toView(): ItemRaceView =
        ItemRaceView(
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

    private fun ItemRaceParticipantEntity.toView(): ItemRaceParticipantView =
        ItemRaceParticipantView(
            id = ItemRaceParticipantId(id),
            raceId = ItemRaceId(raceId),
            accountId = AccountId(accountId),
            accountNickname = accountNickname,
            confirmedAmount = confirmedAmount,
            pendingAmount = pendingAmount,
            status = status,
        )
}
