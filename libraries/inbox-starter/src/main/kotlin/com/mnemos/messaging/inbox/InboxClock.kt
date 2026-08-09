package com.mnemos.messaging.inbox

import java.time.Instant

fun interface InboxClock {

    fun now(): Instant
}