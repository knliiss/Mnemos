package com.mnemos.realtime.application.broadcast.port.out

import com.mnemos.realtime.application.broadcast.model.RealtimeBroadcast

interface ConnectedRealtimeBroadcastSink {
    fun send(broadcast: RealtimeBroadcast)
}
