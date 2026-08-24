package com.mnemos.bet.adapter.out.event

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class EventInternalFeignConfiguration {
    @Bean
    fun eventInternalRequestInterceptor(
        @Value("\${BET_SERVICE_EVENT_API_KEY}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", "bet-service")
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
