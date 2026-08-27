package com.mnemos.playerdirectory.adapter.inbound.scheduling

import com.mnemos.playerdirectory.application.tracking.usecase.poll.PollTrackedPlayersUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.logging.Level
import java.util.logging.Logger

@Component
class PlayerTrackingScheduler(
    private val pollTrackedPlayersUseCase: PollTrackedPlayersUseCase,
    private val logger: Logger = Logger.getLogger(PlayerTrackingScheduler::class.java.name),
) {
    @Scheduled(fixedDelayString = "\${mnemos.player-directory.tracking.scheduler-delay-ms}")
    fun poll() {
        try {
            pollTrackedPlayersUseCase.execute()
        } catch (exception: RuntimeException) {
            logger.log(Level.SEVERE, "Unexpected player tracking scheduler failure", exception)
        }
    }
}
