package com.mnemos.playerdirectory.application.player.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId

data class PlayerNicknameIdentityBatch(
    val playerIdsByNormalizedNickname: Map<String, CristalixPlayerId>,
    val notFoundNormalizedNicknames: Set<String>,
)
