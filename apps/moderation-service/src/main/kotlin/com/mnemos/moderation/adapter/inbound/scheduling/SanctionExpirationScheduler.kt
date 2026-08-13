package com.mnemos.moderation.adapter.inbound.scheduling

import com.mnemos.moderation.application.sanction.usecase.expire.ExpireSanctionsUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SanctionExpirationScheduler(
    private val expireSanctionsUseCase: ExpireSanctionsUseCase,
) {
    @Scheduled(fixedDelayString = "\${mnemos.moderation.expiration-check-interval:30s}")
    fun expire() {
        expireSanctionsUseCase.execute()
    }
}
