package com.mnemos.playerdirectory.adapter.out.http.account

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class PlayerDirectoryAccountFeignConfiguration {
    @Bean
    fun playerDirectoryAccountRequestInterceptor(
        @Value("\${mnemos.player-directory.account-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "player-directory-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
