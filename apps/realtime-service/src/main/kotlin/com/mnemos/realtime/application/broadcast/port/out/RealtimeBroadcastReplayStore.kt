package com.mnemos.realtime.application.broadcast.port.out

import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import java.util.UUID

interface RealtimeBroadcastReplayStore {
    fun append(broadcast: RealtimeBroadcast)

    fun findPending(userId: UUID): List<RealtimeBroadcast>

    fun acknowledge(
        userId: UUID,
        sequence: Long,
    ): Boolean
}
