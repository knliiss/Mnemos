package com.mnemos.personalization.adapter.out.account

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PersonalizationAccountInternalFeignConfiguration {
    @Bean
    fun personalizationAccountInternalRequestInterceptor(
        @Value("\${mnemos.clients.account-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "personalization-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
