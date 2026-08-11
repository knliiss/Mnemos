package com.mnemos.notification.application.notification.policy

import java.time.Duration

data class NotificationTypePolicy(
    val aggregationEnabled: Boolean,
    val aggregationWindow: Duration?,
    val priority: EventPriority,
) {
    init {
        if (aggregationEnabled) {
            require(aggregationWindow != null)
            require(!aggregationWindow.isZero && !aggregationWindow.isNegative)
        } else {
            require(aggregationWindow == null)
        }
    }
}
