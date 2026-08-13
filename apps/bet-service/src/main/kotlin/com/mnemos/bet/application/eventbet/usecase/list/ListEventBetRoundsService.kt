package com.mnemos.bet.application.eventbet.usecase.list

import com.mnemos.bet.application.eventbet.model.EventBetRoundView
import com.mnemos.bet.application.eventbet.port.out.EventBetReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListEventBetRoundsService(
    private val reader: EventBetReader,
) : ListEventBetRoundsUseCase {
    override fun execute(): List<EventBetRoundView> = reader.findCurrentRounds()
}
