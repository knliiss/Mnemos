package com.mnemos.realtime.application.delivery.usecase.replay

import com.mnemos.realtime.application.broadcast.port.out.DropSpotlightPresentationPreferenceStore
import com.mnemos.realtime.application.broadcast.port.out.RealtimeBroadcastReplayStore
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventReplayStore
import org.springframework.stereotype.Service

@Service
class GetRealtimeReplayService(
    private val eventReplayStore: RealtimeEventReplayStore,
    private val broadcastReplayStore: RealtimeBroadcastReplayStore,
    private val presentationPreferenceStore: DropSpotlightPresentationPreferenceStore,
) {
    fun execute(query: GetRealtimeReplayQuery): RealtimeReplay {
        val events =
            query.lastEventId
                ?.let { eventId ->
                    eventReplayStore.findAfter(
                        userId = query.userId,
                        lastEventId = eventId,
                    )
                }.orEmpty()
        val broadcasts = broadcastReplayStore.findPending(query.userId)
        val presentationMode = presentationPreferenceStore.find(query.userId)

        return RealtimeReplay(
            events = events,
            broadcasts = broadcasts,
            dropSpotlightPresentationMode = presentationMode,
        )
    }
}
