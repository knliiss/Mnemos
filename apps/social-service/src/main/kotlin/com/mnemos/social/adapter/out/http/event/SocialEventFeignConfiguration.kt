package com.mnemos.social.adapter.out.http.event

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class SocialEventFeignConfiguration {
    @Bean
    fun socialEventRequestInterceptor(
        @Value("\${mnemos.social.event-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "social-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
