package com.mnemos.playerdirectory.application.accountlink.usecase.project

import com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink
import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Service

@Service
class ProjectPlayerAccountLinkService(
    private val repository: PlayerAccountLinkRepository,
) : ProjectPlayerAccountLinkUseCase {
    override fun execute(command: ProjectPlayerAccountLinkCommand) {
        repository.save(
            PlayerAccountLink(
                playerId = CristalixPlayerId(command.playerId),
                accountId = command.accountId,
                recordedAt = command.recordedAt,
            ),
        )
    }
}
