package com.mnemos.realtime.application.broadcast

import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastFanout
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import org.springframework.stereotype.Service

@Service
class PublishRealtimeBroadcastService(
    private val replayStore: RealtimeBroadcastReplayStore,
    private val fanout: RealtimeBroadcastFanout,
) {
    fun execute(broadcast: RealtimeBroadcast) {
        replayStore.append(broadcast)
        fanout.publish(broadcast)
    }
}
