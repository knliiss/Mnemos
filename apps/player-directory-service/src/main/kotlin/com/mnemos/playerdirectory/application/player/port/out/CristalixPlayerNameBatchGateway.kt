package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.application.player.model.PlayerNicknameObservationBatch
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

fun interface CristalixPlayerNameBatchGateway {
    fun findByNicknames(nicknames: List<PlayerNickname>): PlayerNicknameObservationBatch
}
