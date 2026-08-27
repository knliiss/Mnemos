package com.mnemos.playerdirectory.application.player.usecase.resolve

import com.mnemos.playerdirectory.application.player.exception.PlayerNotFoundException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerGateway
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Service

@Service
class ResolvePlayerByNicknameService(
    private val cristalixPlayerGateway: CristalixPlayerGateway,
    private val observationCoordinator: PlayerObservationCoordinator,
) : ResolvePlayerByNicknameUseCase {
    override fun execute(nickname: PlayerNickname): Player {
        val observation =
            cristalixPlayerGateway.findByNickname(nickname)
                ?: throw PlayerNotFoundException.forNickname(nickname)

        return observationCoordinator.apply(observation)
    }
}
