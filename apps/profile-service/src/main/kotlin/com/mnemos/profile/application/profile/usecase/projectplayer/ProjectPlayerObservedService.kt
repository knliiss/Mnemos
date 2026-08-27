package com.mnemos.profile.application.profile.usecase.projectplayer

import com.mnemos.profile.application.profile.port.out.ProfilePlayerStateRepository
import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectPlayerObservedService(
    private val repository: ProfilePlayerStateRepository,
) : ProjectPlayerObservedUseCase {
    @Transactional
    override fun execute(state: ProfilePlayerState) {
        repository.ensureState(state)

        val current = repository.findByPlayerIdForUpdate(state.playerId) ?: return

        if (state.presenceObservedAt.isBefore(current.presenceObservedAt)) {
            return
        }

        repository.save(
            current.applyPresence(
                nickname = state.nickname,
                externalVersion = state.externalVersion,
                presenceStatus = state.presenceStatus,
                realm = state.realm,
                realmType = state.realmType,
                privacyHidden = state.privacyHidden,
                lastSeenOnline = state.lastSeenOnline,
                observedAt = state.presenceObservedAt,
            ),
        )
    }
}
