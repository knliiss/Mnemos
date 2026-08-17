package com.mnemos.profile.adapter.out.http.account

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class ProfileAccountFeignConfiguration {
    @Bean
    fun profileAccountRequestInterceptor(
        @Value("\${mnemos.profile.account-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "profile-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
