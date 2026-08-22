package com.mnemos.moderation.configuration.wallet

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class WalletServiceFeignConfiguration {
    @Bean
    fun internalApiKeyInterceptor(
        @Value("\${spring.application.name}") serviceName: String,
        @Value("\${mnemos.clients.wallet-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", serviceName)
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
