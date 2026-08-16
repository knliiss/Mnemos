package com.mnemos.realtime.application.broadcast

import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastFanout
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class PublishRealtimeBroadcastService(
    private val replayStore: RealtimeBroadcastReplayStore,
    private val fanout: RealtimeBroadcastFanout,
    private val clock: Clock,
) {
    fun execute(broadcast: RealtimeBroadcast) {
        if (!broadcast.expiresAt.isAfter(clock.instant())) {
            return
        }

        replayStore.append(broadcast)
        fanout.publish(broadcast)
    }
}
