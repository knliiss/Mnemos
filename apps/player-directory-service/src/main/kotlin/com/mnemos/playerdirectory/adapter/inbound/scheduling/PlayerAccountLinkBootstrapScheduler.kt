package com.mnemos.playerdirectory.adapter.inbound.scheduling

import com.mnemos.playerdirectory.application.accountlink.usecase.bootstrap.BootstrapPlayerAccountLinksUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.logging.Level
import java.util.logging.Logger

@Component
class PlayerAccountLinkBootstrapScheduler(
    private val bootstrapPlayerAccountLinksUseCase: BootstrapPlayerAccountLinksUseCase,
    private val logger: Logger = Logger.getLogger(PlayerAccountLinkBootstrapScheduler::class.java.name),
) {
    @Scheduled(
        fixedDelayString = "\${mnemos.player-directory.account-bootstrap.fixed-delay-ms:2000}",
        initialDelayString = "\${mnemos.player-directory.account-bootstrap.initial-delay-ms:5000}",
    )
    fun bootstrap() {
        try {
            bootstrapPlayerAccountLinksUseCase.execute()
        } catch (exception: RuntimeException) {
            logger.log(Level.WARNING, "Player account link bootstrap failed", exception)
        }
    }
}
