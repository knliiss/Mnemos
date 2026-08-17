package com.mnemos.social.adapter.out.http.account

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class SocialAccountFeignConfiguration {
    @Bean
    fun socialAccountRequestInterceptor(
        @Value("\${mnemos.social.account-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "social-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
