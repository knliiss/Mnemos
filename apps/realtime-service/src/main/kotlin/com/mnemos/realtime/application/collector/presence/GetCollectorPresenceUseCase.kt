package com.mnemos.realtime.application.collector.presence

import com.mnemos.realtime.application.collector.model.CollectorPresenceSnapshot

fun interface GetCollectorPresenceUseCase {
    fun execute(): CollectorPresenceSnapshot
}
