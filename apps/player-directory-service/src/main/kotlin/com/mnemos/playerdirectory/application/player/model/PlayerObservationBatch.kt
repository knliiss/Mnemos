package com.mnemos.playerdirectory.application.player.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation

data class PlayerObservationBatch(
    val observations: List<PlayerObservation>,
    val notFound: Set<CristalixPlayerId>,
)
