package com.mnemos.profile.application.profile.usecase.projectplayer

import java.time.Instant
import java.util.UUID

fun interface ProjectPlayerNicknameUseCase {
    fun execute(
        playerId: UUID,
        nickname: String,
        externalVersion: Long,
        observedAt: Instant,
    )
}
