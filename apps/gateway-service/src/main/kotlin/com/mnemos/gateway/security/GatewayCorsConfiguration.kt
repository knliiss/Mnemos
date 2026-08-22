package com.mnemos.gateway.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration(proxyBeanMethods = false)
class GatewayCorsConfiguration {
    @Bean
    fun corsConfigurationSource(
        @Value("\${MNEMOS_FRONTEND_ORIGIN:https://app.knalis.rest}") frontendOrigin: String,
    ): CorsConfigurationSource {
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

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
