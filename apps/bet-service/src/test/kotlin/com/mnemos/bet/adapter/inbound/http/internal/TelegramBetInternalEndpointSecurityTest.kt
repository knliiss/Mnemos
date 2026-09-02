package com.mnemos.bet.adapter.inbound.http.internal

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize

class TelegramBetInternalEndpointSecurityTest {
    @Test
    fun `Telegram bet endpoints are restricted to telegram bot service`() {
        val authorization =
            requireNotNull(
                TelegramEventBetInternalController::class.java.getAnnotation(PreAuthorize::class.java),
            ).value

        assertTrue(authorization.contains("telegram-bot-service"))
        assertFalse(authorization.contains("gateway-service"))
        assertFalse(authorization.contains("payment-service"))
        assertFalse(authorization.contains("notification-service"))
    }
}
