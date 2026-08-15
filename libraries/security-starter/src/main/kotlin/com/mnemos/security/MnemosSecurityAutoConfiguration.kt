package com.mnemos.security

import com.mnemos.security.handler.MnemosAccessDeniedHandler
import com.mnemos.security.handler.MnemosAuthenticationEntryPoint
import com.mnemos.security.handler.SecurityErrorResponseWriter
import com.mnemos.security.internal.InternalApiKeyAuthenticationFilter
import com.mnemos.security.internal.InternalApiKeyAuthenticationFilter.Companion.INTERNAL_SERVICE_AUTHORITY
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import tools.jackson.databind.ObjectMapper

@AutoConfiguration
@EnableMethodSecurity
@EnableConfigurationProperties(MnemosSecurityProperties::class)
@ConditionalOnClass(
    HttpSecurity::class,
    Jwt::class
)
class MnemosSecurityAutoConfiguration {

    @Bean("mnemosCorsConfigurationSource")
    @ConditionalOnMissingBean(name = ["mnemosCorsConfigurationSource"])
    fun corsConfigurationSource(
        @Value("\${MNEMOS_FRONTEND_ORIGIN:https://app.knalis.rest}")
        frontendOrigin: String,
    ): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(
                frontendOrigin
            )
            allowedMethods = listOf(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
            allowedHeaders = listOf(
                "Authorization",
                "Content-Type",
                "Accept",
                "Last-Event-ID"
            )
            maxAge = 3600
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

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
    @Order(1)
    fun mnemosInternalSecurityFilterChain(
        http: HttpSecurity,
        properties: MnemosSecurityProperties,
        authenticationEntryPoint: MnemosAuthenticationEntryPoint,
        accessDeniedHandler: MnemosAccessDeniedHandler
    ): SecurityFilterChain {
        val apiKeyFilter =
            InternalApiKeyAuthenticationFilter(properties.internal)

        http
            .securityMatcher("/internal/**")
            .cors { it.disable() }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.anyRequest().hasAuthority(INTERNAL_SERVICE_AUTHORITY)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .addFilterBefore(
                apiKeyFilter,
                AuthorizationFilter::class.java
            )

        return http.build()
    }

    @Bean
    @Order(2)
    fun mnemosJwtSecurityFilterChain(
        http: HttpSecurity,
        properties: MnemosSecurityProperties,
        jwtAuthenticationConverter: MnemosJwtAuthenticationConverter,
        authenticationEntryPoint: MnemosAuthenticationEntryPoint,
        accessDeniedHandler: MnemosAccessDeniedHandler,
        @Qualifier("mnemosCorsConfigurationSource")
        corsConfigurationSource: CorsConfigurationSource
    ): SecurityFilterChain {
        http
            .cors {
                it.configurationSource(corsConfigurationSource)
            }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { authorization ->
                authorization
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()

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
