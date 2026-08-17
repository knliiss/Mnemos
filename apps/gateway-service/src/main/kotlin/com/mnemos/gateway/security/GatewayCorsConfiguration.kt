package com.mnemos.gateway.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration(proxyBeanMethods = false)
class GatewayCorsConfiguration {
    @Bean
    fun corsWebFilter(
        @Value("\${MNEMOS_FRONTEND_ORIGIN:https://app.knalis.rest}") frontendOrigin: String,
    ): CorsWebFilter {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = listOf(frontendOrigin)
                allowedMethods =
                    listOf(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS",
                    )
                allowedHeaders =
                    listOf(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Last-Event-ID",
                        "Idempotency-Key",
                    )
                allowCredentials = true
                maxAge = 3600
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return CorsWebFilter(source)
    }
}
