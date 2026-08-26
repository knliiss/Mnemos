package com.mnemos.profile.application.profile.usecase.projectplayer

import com.mnemos.profile.application.profile.port.out.ProfilePlayerStateRepository
import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class ProjectPlayerPresenceService(
    private val repository: ProfilePlayerStateRepository,
) : ProjectPlayerPresenceUseCase {
    @Transactional
    override fun execute(
        playerId: UUID,
        nickname: String,
        externalVersion: Long,
        presenceStatus: ProfilePlayerState.PresenceStatus,
        realm: String?,
        realmType: String?,
        privacyHidden: Boolean,
        lastSeenOnline: Instant?,
        observedAt: Instant,
    ) {
        val current = repository.findByPlayerIdForUpdate(playerId) ?: return
        val updated =
            current.applyPresence(
                nickname = nickname,
                externalVersion = externalVersion,
                presenceStatus = presenceStatus,
                realm = realm,
                realmType = realmType,
                privacyHidden = privacyHidden,
                lastSeenOnline = lastSeenOnline,
                observedAt = observedAt,
            )

        if (updated != current) {
            repository.save(updated)
        }
    }
}
