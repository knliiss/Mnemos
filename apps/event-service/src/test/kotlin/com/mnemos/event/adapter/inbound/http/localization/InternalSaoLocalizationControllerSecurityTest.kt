package com.mnemos.event.adapter.inbound.http.localization

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize

class InternalSaoLocalizationControllerSecurityTest {
    @Test
    fun `SAO localization internal endpoint is restricted to notification and telegram bot services`() {
        val authorization =
            requireNotNull(
                InternalSaoLocalizationController::class.java.getAnnotation(PreAuthorize::class.java),
            ).value

        assertTrue(authorization.contains("notification-service"))
        assertTrue(authorization.contains("telegram-bot-service"))
        assertFalse(authorization.contains("social-service"))
        assertFalse(authorization.contains("bet-service"))
    }
}
