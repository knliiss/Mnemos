package com.mnemos.bet.adapter.out.persistence.itemrace

import com.mnemos.bet.adapter.out.persistence.itemrace.ItemRacePersistenceMapper.toView
import com.mnemos.bet.application.itemrace.model.ItemRaceParticipantView
import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import com.mnemos.bet.domain.itemrace.model.ItemRaceId
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
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

    override fun findParticipant(
        raceId: ItemRaceId,
        accountId: AccountId,
    ): ItemRaceParticipantView? =
        participantJpaRepository
            .findByRaceIdAndAccountId(
                raceId = raceId.id,
                accountId = accountId.id,
            )?.toView()

    override fun findOngoingParticipants(accountId: AccountId): List<ItemRaceParticipantView> =
        participantJpaRepository
            .findAllByAccountIdAndStatusInOrderByIdDesc(
                accountId = accountId.id,
                statuses = ongoingParticipantStatuses,
            ).map { it.toView() }

    override fun findHistory(accountId: AccountId): List<ItemRaceParticipantView> =
        participantJpaRepository.findAllByAccountIdOrderByIdDesc(accountId.id).map { it.toView() }

    private companion object {
        val ongoingParticipantStatuses =
            listOf(
                ItemRaceParticipantStatus.PENDING,
                ItemRaceParticipantStatus.ACTIVE,
                ItemRaceParticipantStatus.PAYOUT_PENDING,
                ItemRaceParticipantStatus.PAYOUT_FAILED,
                ItemRaceParticipantStatus.REFUND_PENDING,
                ItemRaceParticipantStatus.REFUND_FAILED,
            )
    }
}
