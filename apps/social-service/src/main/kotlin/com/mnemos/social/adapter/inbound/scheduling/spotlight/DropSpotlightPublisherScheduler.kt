package com.mnemos.social.adapter.inbound.scheduling.spotlight

import com.mnemos.social.application.spotlight.usecase.publish.PublishDueDropSpotlightsUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DropSpotlightPublisherScheduler(
    private val publishDueDropSpotlightsUseCase: PublishDueDropSpotlightsUseCase,
) {
    @Scheduled(fixedDelay = 1_000)
    fun publishDue() {
        publishDueDropSpotlightsUseCase.execute()
    }
}
