package com.mnemos.playerdirectory.application.player.model

import com.mnemos.playerdirectory.domain.player.model.PlayerObservation

data class PlayerNicknameObservationBatch(
    val observationsByNormalizedNickname: Map<String, PlayerObservation>,
    val notFoundNormalizedNicknames: Set<String>,
)
