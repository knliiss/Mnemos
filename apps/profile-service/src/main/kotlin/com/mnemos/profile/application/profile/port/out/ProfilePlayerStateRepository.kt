package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import java.util.UUID

interface ProfilePlayerStateRepository {
    fun ensureState(state: ProfilePlayerState)

    fun save(state: ProfilePlayerState): ProfilePlayerState

    fun findByPlayerId(playerId: UUID): ProfilePlayerState?

    fun findByPlayerIdForUpdate(playerId: UUID): ProfilePlayerState?

    fun findByPlayerIds(playerIds: Collection<UUID>): Map<UUID, ProfilePlayerState>
}
