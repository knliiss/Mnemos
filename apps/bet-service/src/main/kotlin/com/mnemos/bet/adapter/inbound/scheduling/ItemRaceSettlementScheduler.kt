package com.mnemos.bet.adapter.inbound.scheduling

import com.mnemos.bet.application.itemrace.usecase.settle.SynchronizeItemRacesUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ItemRaceSettlementScheduler(
    private val synchronizeItemRacesUseCase: SynchronizeItemRacesUseCase,
) {
    @Scheduled(fixedDelayString = "\${mnemos.bet.scheduler.item-race-settlement-delay:5s}")
    fun synchronize() {
        synchronizeItemRacesUseCase.execute()
    }
}
