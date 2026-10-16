package com.mnemos.playerdirectory.application.accountlink.usecase.project

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink
import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class ProjectPlayerAccountLinkService(
    private val repository: PlayerAccountLinkRepository,
    private val trackingRepository: PlayerTrackingRepository,
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
        trackingRepository.ensureState(
            PlayerTrackingState.create(
                playerId = CristalixPlayerId(command.playerId),
                nextPollAt = now,
                createdAt = now,
            ),
        )
    }
}
