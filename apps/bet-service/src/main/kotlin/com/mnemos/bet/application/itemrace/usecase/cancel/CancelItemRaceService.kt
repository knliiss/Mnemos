package com.mnemos.bet.application.itemrace.usecase.cancel

import com.mnemos.bet.application.itemrace.service.ItemRaceRefundProcessor
import com.mnemos.bet.domain.exception.BetNotFoundException
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CancelItemRaceService(
    private val repository: ItemRaceRepository,
    private val refundProcessor: ItemRaceRefundProcessor,
) : CancelItemRaceUseCase {
    @Transactional
    override fun execute(command: CancelItemRaceCommand) {
        val race =
            repository.findByIdForUpdate(command.raceId)
                ?: throw BetNotFoundException("Item race ${command.raceId} was not found")

        race.cancel()
        repository.save(race)

        refundProcessor.refund(
            race = race,
            participants = repository.findParticipantsByRaceForUpdate(race.id),
        )
    }
}
