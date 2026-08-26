package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.application.player.model.PlayerObservationBatch
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId

fun interface CristalixPlayerBatchGateway {
    fun findByIds(playerIds: List<CristalixPlayerId>): PlayerObservationBatch
}
