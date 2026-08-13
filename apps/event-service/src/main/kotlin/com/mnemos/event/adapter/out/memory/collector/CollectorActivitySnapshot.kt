package com.mnemos.event.adapter.out.memory.collector

import java.time.Instant

data class CollectorActivitySnapshot(
    val occurredAt: Instant,
    val activeCollectors: Int,
)
