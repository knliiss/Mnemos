package com.mnemos.playerdirectory.application.player.support

import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlayerObservationCoordinator(
    private val playerRepository: PlayerRepository,
) {
    @Transactional
    fun apply(observation: PlayerObservation): Player {
        val current = playerRepository.findByIdForUpdate(observation.playerId)
        val player = current?.applyObservation(observation) ?: Player.fromObservation(observation)

        if (player == current) {
            return current
        }

        return playerRepository.save(player)
    }
}
