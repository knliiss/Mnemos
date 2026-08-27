package com.mnemos.account.persistence.repository.player

import com.mnemos.account.application.port.out.PlayerIdentityRevisionStore
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PlayerIdentityRevisionStoreAdapter(
    private val repository: JpaPlayerIdentityRevisionRepository,
) : PlayerIdentityRevisionStore {
    override fun advanceIfNewer(
        playerId: CristalixUuid,
        nickname: CristalixNickname,
        externalVersion: Long,
        observedAt: Instant,
        recordedAt: Instant,
    ): Boolean =
        repository.advanceIfNewer(
            playerId = playerId.id,
            nickname = nickname.value,
            externalVersion = externalVersion,
            observedAt = observedAt,
            recordedAt = recordedAt,
        ) == 1
}
