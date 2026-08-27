package com.mnemos.playerdirectory.application.activity.usecase.observe

import com.mnemos.playerdirectory.application.activity.model.PlayerEventActivityType
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import java.time.Instant

interface ObservePlayerEventActivityUseCase {
    fun execute(
        nickname: PlayerNickname,
        occurredAt: Instant,
        type: PlayerEventActivityType,
    )
}
