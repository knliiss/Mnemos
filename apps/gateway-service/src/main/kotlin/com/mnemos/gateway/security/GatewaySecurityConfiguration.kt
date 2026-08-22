package com.mnemos.gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.reactive.CorsConfigurationSource

@Configuration(proxyBeanMethods = false)
class GatewaySecurityConfiguration(
    private val corsConfigurationSource: CorsConfigurationSource,
) {
    @Bean
    @Order(1)
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource)
            }.authorizeExchange {
                it.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                it.pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                it.pathMatchers("/ws/v1/collector").permitAll()
                it.anyExchange().authenticated()
            }.oauth2ResourceServer {
                it.jwt { }
            }.build()
}
