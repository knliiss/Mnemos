package com.mnemos.playerdirectory.application.player.exception

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

class PlayerNotFoundException private constructor(
    message: String,
) : RuntimeException(message) {
    constructor(playerId: CristalixPlayerId) : this("Cristalix player ${playerId.id} was not found")

    companion object {
        fun forNickname(nickname: PlayerNickname): PlayerNotFoundException =
            PlayerNotFoundException("Cristalix player ${nickname.value} was not found")
    }
}
