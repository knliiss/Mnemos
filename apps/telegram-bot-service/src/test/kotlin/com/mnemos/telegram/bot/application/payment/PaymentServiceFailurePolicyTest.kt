package com.mnemos.telegram.bot.application.payment

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PaymentServiceFailurePolicyTest {
    @Test
    fun `deterministic client failures are permanent`() {
        assertTrue(PaymentServiceFailurePolicy.isPermanent(400))
        assertTrue(PaymentServiceFailurePolicy.isPermanent(404))
        assertTrue(PaymentServiceFailurePolicy.isPermanent(409))
        assertTrue(PaymentServiceFailurePolicy.isPermanent(422))
    }

    @Test
    fun `authentication throttling and transient failures remain retryable`() {
        assertFalse(PaymentServiceFailurePolicy.isPermanent(401))
        assertFalse(PaymentServiceFailurePolicy.isPermanent(403))
        assertFalse(PaymentServiceFailurePolicy.isPermanent(408))
        assertFalse(PaymentServiceFailurePolicy.isPermanent(425))
        assertFalse(PaymentServiceFailurePolicy.isPermanent(429))
        assertFalse(PaymentServiceFailurePolicy.isPermanent(503))
        assertFalse(PaymentServiceFailurePolicy.isPermanent(-1))
    }
}
