package com.mnemos.event.application.collector.verification

import com.mnemos.event.configuration.collector.EventVerificationProperties
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class EventConfirmationPolicy(
    private val properties: EventVerificationProperties,
) {
    fun requiredConfirmations(activeCollectors: Int): Int {
        require(activeCollectors > 0)

        val percentageRequired =
            ceil(activeCollectors * properties.confirmationRatio).toInt()

        return minOf(
            activeCollectors,
            maxOf(
                properties.minimumConfirmations,
                percentageRequired,
            ),
        )
    }
}
