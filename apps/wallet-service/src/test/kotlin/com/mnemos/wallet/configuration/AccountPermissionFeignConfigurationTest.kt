package com.mnemos.wallet.configuration

import feign.RequestTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccountPermissionFeignConfigurationTest {
    @Test
    fun `internal interceptor adds service identity and api key headers`() {
        val interceptor =
            AccountPermissionFeignConfiguration().internalApiKeyInterceptor(
                serviceName = "wallet-service",
                apiKey = "test-api-key",
            )
        val template = RequestTemplate()

        interceptor.apply(template)

        assertEquals(
            listOf("wallet-service"),
            template.headers()["X-Mnemos-Service"]?.toList(),
        )
        assertEquals(
            listOf("test-api-key"),
            template.headers()["X-Mnemos-API-Key"]?.toList(),
        )
    }
}
