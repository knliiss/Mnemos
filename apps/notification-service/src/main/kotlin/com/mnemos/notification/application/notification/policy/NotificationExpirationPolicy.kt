package com.mnemos.notification.application.notification.policy

import java.time.Duration
import java.time.Instant

class NotificationExpirationPolicy(
    private val ttl: Duration,
) {
    init {
        require(!ttl.isZero && !ttl.isNegative)
    }

    fun expiresAt(occurredAt: Instant): Instant = occurredAt.plus(ttl)
}
