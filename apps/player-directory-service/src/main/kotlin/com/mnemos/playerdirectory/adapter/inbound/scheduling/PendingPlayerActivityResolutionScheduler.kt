package com.mnemos.playerdirectory.adapter.inbound.scheduling

import com.mnemos.playerdirectory.application.activity.usecase.resolve.ResolvePendingPlayerActivityUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.logging.Level
import java.util.logging.Logger

@Component
class PendingPlayerActivityResolutionScheduler(
    private val resolvePendingPlayerActivityUseCase: ResolvePendingPlayerActivityUseCase,
    private val logger: Logger = Logger.getLogger(PendingPlayerActivityResolutionScheduler::class.java.name),
) {
    @Scheduled(fixedDelayString = "\${mnemos.player-directory.resolution.scheduler-delay-ms}")
    fun resolve() {
        try {
            resolvePendingPlayerActivityUseCase.execute()
        } catch (exception: RuntimeException) {
            logger.log(Level.SEVERE, "Unexpected pending player resolution scheduler failure", exception)
        }
    }
}
