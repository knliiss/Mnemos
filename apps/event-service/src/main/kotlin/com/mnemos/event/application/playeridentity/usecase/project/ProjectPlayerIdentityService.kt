package com.mnemos.event.application.playeridentity.usecase.project

import com.mnemos.event.application.playeridentity.model.PlayerIdentityObservation
import com.mnemos.event.application.playeridentity.policy.PlayerIdentityResolutionPolicy
import com.mnemos.event.application.playeridentity.port.out.PendingEventIdentityRepository
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Service

@Service
class ProjectPlayerIdentityService(
    private val repository: PlayerIdentityProjectionRepository,
    private val pendingEventIdentityRepository: PendingEventIdentityRepository,
    private val resolutionPolicy: PlayerIdentityResolutionPolicy,
) : ProjectPlayerIdentityUseCase {
    override fun execute(command: ProjectPlayerIdentityCommand) {
        val observation =
            PlayerIdentityObservation(
                playerId = command.playerId,
                nickname = command.nickname,
                externalVersion = command.externalVersion,
                observedAt = command.observedAt,
            )

        if (!repository.apply(observation)) {
            return
        }

        pendingEventIdentityRepository.reconcile(
            nickname = observation.nickname,
            playerId = CristalixPlayerId(observation.playerId),
            observedAt = observation.observedAt,
            maxAge = resolutionPolicy.maxAge,
        )
    }
}
