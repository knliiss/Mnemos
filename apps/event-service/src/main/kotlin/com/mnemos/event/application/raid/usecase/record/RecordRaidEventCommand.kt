package com.mnemos.event.application.raid.usecase.record

import java.time.Instant

data class RecordRaidEventCommand(
    val locations: Set<Int>,
    val startedAt: Instant,
)
