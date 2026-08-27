package com.mnemos.playerdirectory.application.player.port.out

import java.time.Duration

fun interface PlayerPresenceTelemetry {
    fun recordOfflineDetectionLatency(latency: Duration)
}
