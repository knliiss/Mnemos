package com.mnemos.playerdirectory.application.accountlink.usecase.project

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink
import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class ProjectPlayerAccountLinkService(
    private val repository: PlayerAccountLinkRepository,
    private val trackingRepository: PlayerTrackingRepository,
    private val playerRepository: PlayerRepository,
    private val eventPublisher: PlayerIntegrationEventPublisher,
    private val clock: Clock,
) : ProjectPlayerAccountLinkUseCase {
    @Transactional
    override fun execute(command: ProjectPlayerAccountLinkCommand) {
        repository.save(
            PlayerAccountLink(
                playerId = CristalixPlayerId(command.playerId),
                accountId = command.accountId,
                recordedAt = command.recordedAt,
            ),
        )
        // A linked Cristalix account must stay eligible for periodic identity refresh,
        // even when it has no drop activity or presence subscriptions.
        val now = Instant.now(clock)
        val playerId = CristalixPlayerId(command.playerId)
        trackingRepository.ensureState(
            PlayerTrackingState.create(
                playerId = playerId,
                nextPollAt = now,
                createdAt = now,
            ),
        )

        // Backfilled accounts may have missed an earlier observation.
        // Re-broadcast the known identity so account-service can reconcile
        // stale names even when the external version has not changed.
        playerRepository.findById(playerId)?.let { player ->
            eventPublisher.publishInitial(player, player.presence.observedAt)
        }
    }
}
