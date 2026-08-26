package com.mnemos.playerdirectory.application.activity.usecase.observe

import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import java.time.Instant

fun interface ObserveItemDropActivityUseCase {
    fun execute(
        nickname: PlayerNickname,
        occurredAt: Instant,
    )
}
