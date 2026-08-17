package com.mnemos.social.adapter.out.http.personalization

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class SocialPersonalizationFeignConfiguration {
    @Bean
    fun socialPersonalizationRequestInterceptor(
        @Value("\${mnemos.social.personalization-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "social-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
