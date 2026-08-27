package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation

interface CristalixPlayerGateway {
    fun findById(playerId: CristalixPlayerId): PlayerObservation?

    fun findByNickname(nickname: PlayerNickname): PlayerObservation?
}
