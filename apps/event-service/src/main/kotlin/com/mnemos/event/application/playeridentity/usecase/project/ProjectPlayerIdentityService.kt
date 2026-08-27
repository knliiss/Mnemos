package com.mnemos.event.application.playeridentity.usecase.project

import com.mnemos.event.application.playeridentity.model.PlayerIdentityObservation
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import org.springframework.stereotype.Service

@Service
class ProjectPlayerIdentityService(
    private val repository: PlayerIdentityProjectionRepository,
) : ProjectPlayerIdentityUseCase {
    override fun execute(command: ProjectPlayerIdentityCommand) {
        repository.apply(
            PlayerIdentityObservation(
                playerId = command.playerId,
                nickname = command.nickname,
                externalVersion = command.externalVersion,
                observedAt = command.observedAt,
            ),
        )
    }
}
