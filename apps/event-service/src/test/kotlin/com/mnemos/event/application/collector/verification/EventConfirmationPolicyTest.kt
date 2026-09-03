package com.mnemos.event.application.collector.verification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EventConfirmationPolicyTest {
    private val policy = EventConfirmationPolicy()

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 5, 20])
    fun `global active collector count does not create a cross-stream quorum`(activeCollectors: Int) {
        assertEquals(
            1,
            policy.requiredConfirmations(activeCollectors),
        )
    }
}
