package com.mnemos.bet.application.itemrace.usecase.settle

import com.mnemos.bet.application.event.port.out.ItemDropHistoryProvider
import com.mnemos.bet.application.itemrace.service.ItemRaceRefundProcessor
import com.mnemos.bet.application.itemrace.service.ItemRaceSettlementProcessor
import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipant
import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class SynchronizeItemRacesService(
    private val repository: ItemRaceRepository,
    private val itemDropHistoryProvider: ItemDropHistoryProvider,
    private val settlementProcessor: ItemRaceSettlementProcessor,
    private val refundProcessor: ItemRaceRefundProcessor,
    private val clock: Clock,
) : SynchronizeItemRacesUseCase {
    @Transactional
    override fun execute() {
        val now = clock.instant()

        repository
            .findRacesRequiringSynchronization(now)
            .forEach { race -> synchronize(race, now) }
    }

    private fun synchronize(
        race: ItemRace,
        now: Instant,
    ) {
        val participants = repository.findParticipantsByRaceForUpdate(race.id)

        if (race.status == ItemRaceStatus.REGISTRATION && !now.isBefore(race.startsAt)) {
            activateOrRefund(
                race = race,
                participants = participants,
            )
        }

        if (race.status == ItemRaceStatus.ACTIVE) {
            settleActiveRace(
                race = race,
                participants = participants,
                now = now,
            )
        }

        if (race.status == ItemRaceStatus.REFUNDING) {
            completeRefundIfPossible(
                race = race,
                participants = participants,
            )
        }
    }

    private fun activateOrRefund(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
    ) {
        val confirmedParticipants = participants.count { it.confirmedAmount > 0 }

        if (confirmedParticipants < MINIMUM_PARTICIPANTS) {
            race.beginRefund()
            repository.save(race)

            refundProcessor.refund(
                race = race,
                participants = participants,
            )
            return
        }

        race.activate()
        repository.save(race)
    }

    private fun settleActiveRace(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
        now: Instant,
    ) {
        val lookupEnd = if (now.isBefore(race.expiresAt)) now else race.expiresAt
        val firstDrop =
            itemDropHistoryProvider.findFirst(
                itemName = race.itemName,
                itemType = race.itemType,
                from = race.startsAt,
                to = lookupEnd,
            )

        if (firstDrop != null) {
            settlementProcessor.settleFromDrop(
                race = race,
                participants = participants,
                droppedFor = firstDrop.droppedFor,
            )
            return
        }

        if (!now.isBefore(race.expiresAt)) {
            race.beginRefund()
            repository.save(race)

            refundProcessor.refund(
                race = race,
                participants = participants,
            )
        }
    }

    private fun completeRefundIfPossible(
        race: ItemRace,
        participants: List<ItemRaceParticipant>,
    ) {
        val refundableParticipants = participants.filter { it.confirmedAmount > 0 }

        if (refundableParticipants.all { it.status == ItemRaceParticipantStatus.REFUNDED }) {
            race.completeRefund()
            repository.save(race)
        }
    }

    companion object {
        const val MINIMUM_PARTICIPANTS = 2
    }
}
