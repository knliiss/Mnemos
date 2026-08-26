package com.mnemos.profile.application.profile.usecase.projectplayer

import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import java.time.Instant
import java.util.UUID

fun interface ProjectPlayerPresenceUseCase {
    fun execute(
        playerId: UUID,
        nickname: String,
        externalVersion: Long,
        presenceStatus: ProfilePlayerState.PresenceStatus,
        realm: String?,
        realmType: String?,
        privacyHidden: Boolean,
        lastSeenOnline: Instant?,
        observedAt: Instant,
    )
}
