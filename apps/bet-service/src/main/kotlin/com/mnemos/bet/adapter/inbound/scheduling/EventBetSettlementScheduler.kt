package com.mnemos.bet.adapter.inbound.scheduling

import com.mnemos.bet.application.eventbet.usecase.settle.SynchronizeEventBetRoundsUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class EventBetSettlementScheduler(
    private val synchronizeEventBetRoundsUseCase: SynchronizeEventBetRoundsUseCase,
) {
    @Scheduled(fixedDelayString = "\${mnemos.bet.scheduler.event-settlement-delay:5s}")
    fun synchronize() {
        synchronizeEventBetRoundsUseCase.execute()
    }
}
