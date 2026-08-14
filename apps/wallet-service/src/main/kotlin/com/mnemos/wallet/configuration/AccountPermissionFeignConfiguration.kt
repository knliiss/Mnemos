package com.mnemos.wallet.configuration

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class AccountPermissionFeignConfiguration {
    @Bean
    fun internalApiKeyInterceptor(
        @Value("\${mnemos.clients.account-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Internal-Api-Key", apiKey)
        }
}
