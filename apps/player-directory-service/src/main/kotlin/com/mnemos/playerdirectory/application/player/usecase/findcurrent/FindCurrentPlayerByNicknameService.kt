package com.mnemos.playerdirectory.application.player.usecase.findcurrent

import com.mnemos.playerdirectory.application.player.port.out.PlayerNicknameOwnershipRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import org.springframework.stereotype.Service

@Service
class FindCurrentPlayerByNicknameService(
    private val nicknameOwnershipRepository: PlayerNicknameOwnershipRepository,
    private val playerRepository: PlayerRepository,
) : FindCurrentPlayerByNicknameUseCase {
    override fun execute(nickname: PlayerNickname): Player? {
        val ownership = nicknameOwnershipRepository.findByNickname(nickname) ?: return null

        return playerRepository.findById(ownership.playerId)
    }
}
