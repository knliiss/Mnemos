package com.mnemos.playerdirectory.application.player.usecase.findcurrent

import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

fun interface FindCurrentPlayerByNicknameUseCase {
    fun execute(nickname: PlayerNickname): Player?
}
