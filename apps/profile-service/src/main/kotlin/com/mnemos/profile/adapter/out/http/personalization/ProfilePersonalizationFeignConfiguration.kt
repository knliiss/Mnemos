package com.mnemos.profile.adapter.out.http.personalization

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class ProfilePersonalizationFeignConfiguration {
    @Bean
    fun profilePersonalizationRequestInterceptor(
        @Value("\${mnemos.profile.personalization-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "profile-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
