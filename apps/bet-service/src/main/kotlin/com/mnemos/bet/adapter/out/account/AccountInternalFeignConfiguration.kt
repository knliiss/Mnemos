package com.mnemos.bet.adapter.out.account

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class AccountInternalFeignConfiguration {
    @Bean
    fun accountInternalRequestInterceptor(
        @Value("\${BET_SERVICE_ACCOUNT_API_KEY}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "bet-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
