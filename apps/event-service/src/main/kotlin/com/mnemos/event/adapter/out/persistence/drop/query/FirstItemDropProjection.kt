package com.mnemos.event.adapter.out.persistence.drop.query

import java.time.Instant

interface FirstItemDropProjection {
    fun getDroppedFor(): String

    fun getOccurredAt(): Instant
}
