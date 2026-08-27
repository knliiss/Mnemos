package com.mnemos.playerdirectory.application.player.usecase.resolve

import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname

fun interface ResolvePlayerByNicknameUseCase {
    fun execute(nickname: PlayerNickname): Player
}
