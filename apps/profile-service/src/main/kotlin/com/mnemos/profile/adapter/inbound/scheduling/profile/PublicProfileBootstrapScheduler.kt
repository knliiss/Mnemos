package com.mnemos.profile.adapter.inbound.scheduling.profile

import com.mnemos.profile.application.profile.usecase.bootstrap.BootstrapPublicProfilesUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PublicProfileBootstrapScheduler(
    private val bootstrapPublicProfilesUseCase: BootstrapPublicProfilesUseCase,
) {
    @Scheduled(fixedDelayString = "\${mnemos.profile.bootstrap.fixed-delay-ms:2000}")
    fun bootstrap() {
        bootstrapPublicProfilesUseCase.execute()
    }
}
