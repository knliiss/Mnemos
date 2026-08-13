package com.mnemos.gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration(proxyBeanMethods = false)
class GatewaySecurityConfiguration {
    @Bean
    @Order(1)
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .cors { }
            .authorizeExchange {
                it.pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                it.pathMatchers("/ws/v1/collector").permitAll()
                it.anyExchange().authenticated()
            }.oauth2ResourceServer {
                it.jwt { }
            }.build()
}
