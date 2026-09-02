package com.mnemos.telegram.bot.adapter.out.settings

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class PersonalizationServiceFeignConfiguration {
    @Bean
    fun internalApiKeyInterceptor(
        @Value("\${spring.application.name}") serviceName: String,
        @Value("\${mnemos.clients.personalization-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", serviceName)
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
