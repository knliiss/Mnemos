package com.mnemos.gateway.routing

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GatewayAdminRouteConfigurationTest {
    @Test
    fun `delegated admin account routes precede account fallback`() {
        val configuration =
            requireNotNull(javaClass.classLoader.getResource("application.yml")) {
                "application.yml must be available on the test classpath"
            }.readText()

        val walletAdminRoute = configuration.indexOf("- id: wallet-admin-account-transactions")
        val personalizationAdminRoute = configuration.indexOf("- id: personalization-admin-account")
        val accountFallbackRoute = configuration.indexOf("- id: account-api")

        assertTrue(walletAdminRoute >= 0)
        assertTrue(personalizationAdminRoute >= 0)
        assertTrue(accountFallbackRoute >= 0)
        assertTrue(walletAdminRoute < accountFallbackRoute)
        assertTrue(personalizationAdminRoute < accountFallbackRoute)
        assertTrue(
            configuration.contains(
                "Path=/api/v1/admin/accounts/*/transactions,/api/v1/admin/accounts/*/transactions/**",
            ),
        )
        assertTrue(
            configuration.contains(
                "Path=/api/v1/admin/accounts/*/personalizations,/api/v1/admin/accounts/*/personalizations/**",
            ),
        )
    }
}
