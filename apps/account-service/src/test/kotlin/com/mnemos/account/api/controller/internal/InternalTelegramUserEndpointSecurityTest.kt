package com.mnemos.account.api.controller.internal

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize

class InternalTelegramUserEndpointSecurityTest {
    @Test
    fun `full Telegram user context is restricted to reward service`() {
        val authorization = authorizationFor(InternalTelegramUserContextController::class.java)

        assertTrue(authorization.contains("reward-service"))
        assertFalse(authorization.contains("payment-service"))
        assertFalse(authorization.contains("personalization-service"))
    }

    @Test
    fun `narrow Telegram identity is available to payment and personalization only`() {
        val authorization = authorizationFor(InternalTelegramUserIdentityController::class.java)

        assertTrue(authorization.contains("payment-service"))
        assertTrue(authorization.contains("personalization-service"))
        assertFalse(authorization.contains("reward-service"))
    }

    @Test
    fun `Telegram identity bootstrap is restricted to payment service`() {
        val method =
            InternalTelegramUserIdentityController::class.java.getDeclaredMethod(
                "bootstrapIdentity",
                java.lang.Long.TYPE,
            )
        val authorization = requireNotNull(method.getAnnotation(PreAuthorize::class.java)).value

        assertTrue(authorization.contains("payment-service"))
        assertFalse(authorization.contains("personalization-service"))
        assertFalse(authorization.contains("telegram-bot-service"))
    }

    @Test
    fun `Telegram user settings are restricted to telegram bot service`() {
        val authorization = authorizationFor(InternalTelegramUserSettingsController::class.java)

        assertTrue(authorization.contains("telegram-bot-service"))
        assertFalse(authorization.contains("payment-service"))
        assertFalse(authorization.contains("personalization-service"))
        assertFalse(authorization.contains("reward-service"))
    }

    private fun authorizationFor(controllerType: Class<*>): String =
        requireNotNull(controllerType.getAnnotation(PreAuthorize::class.java)).value
}
