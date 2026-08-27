package com.mnemos.playerdirectory.application.player.port.out

import com.mnemos.playerdirectory.application.player.model.PlayerNicknameIdentityBatch
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

fun interface CristalixPlayerNameBatchGateway {
    fun findByNicknames(nicknames: List<PlayerNickname>): PlayerNicknameIdentityBatch
}
