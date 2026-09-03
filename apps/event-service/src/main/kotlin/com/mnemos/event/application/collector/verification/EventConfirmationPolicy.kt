package com.mnemos.event.application.collector.verification

import org.springframework.stereotype.Component

@Component
class EventConfirmationPolicy {
    fun requiredConfirmations(activeCollectors: Int): Int {
        require(activeCollectors > 0)

        return 1
    }
}
