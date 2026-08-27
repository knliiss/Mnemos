package com.mnemos.playerdirectory.adapter.out.persistence.observability

import com.mnemos.playerdirectory.application.observability.model.PlayerDirectoryOperationalSnapshot
import com.mnemos.playerdirectory.application.observability.port.out.PlayerDirectoryOperationalSnapshotReader
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
class PlayerDirectoryOperationalSnapshotReaderAdapter(
    private val repository: PlayerDirectoryOperationalSnapshotJpaRepository,
) : PlayerDirectoryOperationalSnapshotReader {
    @Transactional(readOnly = true)
    override fun read(
        now: Instant,
        staleBefore: Instant,
    ): PlayerDirectoryOperationalSnapshot {
        val projection =
            repository.readSnapshot(
                now = now,
                staleBefore = staleBefore,
            )

        return PlayerDirectoryOperationalSnapshot(
            trackingDueCount = projection.trackingDueCount,
            oldestTrackingDueAt = projection.oldestTrackingDueAt,
            resolutionQueueCount = projection.resolutionQueueCount,
            trackedOnlineCount = projection.trackedOnlineCount,
            trackedSaoCount = projection.trackedSaoCount,
            stalePresenceCount = projection.stalePresenceCount,
        )
    }
}
