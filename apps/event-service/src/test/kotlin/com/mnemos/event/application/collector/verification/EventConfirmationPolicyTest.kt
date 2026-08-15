package com.mnemos.event.application.collector.verification

import com.mnemos.event.configuration.collector.EventVerificationProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EventConfirmationPolicyTest {
    private val policy = EventConfirmationPolicy(EventVerificationProperties())

    @ParameterizedTest
    @CsvSource(
        "1, 1",
        "2, 2",
        "3, 2",
        "4, 2",
        "5, 3",
    )
    fun `balanced quorum keeps a single trusted collector available`(
        activeCollectors: Int,
        expectedConfirmations: Int,
    ) {
        assertEquals(
            expectedConfirmations,
            policy.requiredConfirmations(activeCollectors),
        )
    }
}
