package com.mnemos.wallet.integration.account.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class AccountPermissionFeignConfiguration {
    @Bean
    fun accountInternalApiKeyInterceptor(
        @Value("\${spring.application.name}") serviceName: String,
        @Value("\${mnemos.clients.account-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", serviceName)
            template.header("X-Mnemos-Api-Key", apiKey)
        }
}
