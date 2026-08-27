package com.mnemos.account.application.port.out

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import java.time.Instant

interface PlayerIdentityRevisionStore {
    fun advanceIfNewer(
        playerId: CristalixUuid,
        nickname: CristalixNickname,
        externalVersion: Long,
        observedAt: Instant,
        recordedAt: Instant,
    ): Boolean
}
