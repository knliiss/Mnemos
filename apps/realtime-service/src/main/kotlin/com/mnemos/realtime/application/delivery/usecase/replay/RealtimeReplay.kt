package com.mnemos.realtime.application.delivery.usecase.replay

import com.mnemos.realtime.application.broadcast.model.DropSpotlightPresentationMode
import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast
import com.mnemos.realtime.application.delivery.model.RealtimeEvent

data class RealtimeReplay(
    val events: List<RealtimeEvent>,
    val broadcasts: List<RealtimeBroadcast>,
    val dropSpotlightPresentationMode: DropSpotlightPresentationMode,
)
