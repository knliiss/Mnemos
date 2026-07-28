package com.mnemos.messaging.outbox.support

import java.util.UUID

fun interface OutboxEventIdGenerator {
    fun generate(): UUID
}