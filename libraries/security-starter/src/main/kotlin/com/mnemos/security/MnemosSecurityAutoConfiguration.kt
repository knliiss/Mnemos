package com.mnemos.security

import com.mnemos.security.handler.MnemosAccessDeniedHandler
import com.mnemos.security.handler.MnemosAuthenticationEntryPoint
import com.mnemos.security.handler.SecurityErrorResponseWriter
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain
import tools.jackson.databind.ObjectMapper

@AutoConfiguration
@EnableMethodSecurity
@EnableConfigurationProperties(MnemosSecurityProperties::class)
@ConditionalOnClass(
    HttpSecurity::class,
    Jwt::class
)
class MnemosSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun mnemosJwtAuthenticationConverter(
        properties: MnemosSecurityProperties
    ) = MnemosJwtAuthenticationConverter(properties)

    @Bean
    @ConditionalOnMissingBean
    fun currentMnemosUser(
        properties: MnemosSecurityProperties
    ) = CurrentMnemosUser(properties)

    @Bean
    @ConditionalOnMissingBean
    fun securityErrorResponseWriter(
        objectMapper: ObjectMapper
    ) = SecurityErrorResponseWriter(objectMapper)

    @Bean
    @ConditionalOnMissingBean
    fun mnemosAuthenticationEntryPoint(
        responseWriter: SecurityErrorResponseWriter
    ) = MnemosAuthenticationEntryPoint(responseWriter)

    @Bean
    @ConditionalOnMissingBean
    fun mnemosAccessDeniedHandler(
        responseWriter: SecurityErrorResponseWriter
    ) = MnemosAccessDeniedHandler(responseWriter)

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain::class)
    fun mnemosSecurityFilterChain(
        http: HttpSecurity,
        properties: MnemosSecurityProperties,
        jwtAuthenticationConverter: MnemosJwtAuthenticationConverter,
        authenticationEntryPoint: MnemosAuthenticationEntryPoint,
        accessDeniedHandler: MnemosAccessDeniedHandler
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorization ->
                properties.publicPaths.forEach { path ->
                    authorization.requestMatchers(path).permitAll()
                }

                authorization.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.authenticationEntryPoint(authenticationEntryPoint)
                resourceServer.accessDeniedHandler(accessDeniedHandler)
                resourceServer.jwt {
                    it.jwtAuthenticationConverter(jwtAuthenticationConverter)
                }
            }

        return http.build()
    }
}