package com.mnemos.playerdirectory.application.player.exception

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

class PlayerNotFoundException private constructor(
    message: String,
) : RuntimeException(message) {
    constructor(playerId: CristalixPlayerId) : this("Cristalix player ${playerId.id} was not found")

    constructor(nickname: PlayerNickname) : this("Cristalix player $nickname was not found")
}
