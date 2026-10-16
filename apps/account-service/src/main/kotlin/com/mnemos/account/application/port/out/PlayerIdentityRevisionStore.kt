package com.mnemos.account.application.port.out

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import java.time.Instant

interface PlayerIdentityRevisionStore {
    /** Allows repairing account snapshots after a missed or out-of-order event. */
    fun matchesCurrent(
        playerId: CristalixUuid,
        nickname: CristalixNickname,
        externalVersion: Long,
    ): Boolean

    fun advanceIfNewer(
        playerId: CristalixUuid,
        nickname: CristalixNickname,
        externalVersion: Long,
        observedAt: Instant,
        recordedAt: Instant,
    ): Boolean
}
