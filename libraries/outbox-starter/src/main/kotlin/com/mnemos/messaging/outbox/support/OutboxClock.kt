package com.mnemos.messaging.outbox.support

import java.time.Instant

fun interface OutboxClock {
    fun now(): Instant
}