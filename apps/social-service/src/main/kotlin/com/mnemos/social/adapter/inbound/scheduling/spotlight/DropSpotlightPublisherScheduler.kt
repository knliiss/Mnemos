package com.mnemos.social.adapter.inbound.scheduling.spotlight

import com.mnemos.social.application.spotlight.PublishDueDropSpotlightsService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class DropSpotlightPublisherScheduler(
    private val service: PublishDueDropSpotlightsService,
) {
    @Scheduled(fixedDelay = 1_000)
    fun publishDue() {
        service.execute()
    }
}
