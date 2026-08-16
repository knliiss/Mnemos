package com.mnemos.profile.adapter.inbound.scheduling.profile

import com.mnemos.profile.application.profile.bootstrap.PublicProfileBootstrapService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PublicProfileBootstrapScheduler(
    private val service: PublicProfileBootstrapService,
) {
    @Scheduled(fixedDelayString = "\${mnemos.profile.bootstrap.fixed-delay-ms:2000}")
    fun bootstrap() {
        service.execute()
    }
}
