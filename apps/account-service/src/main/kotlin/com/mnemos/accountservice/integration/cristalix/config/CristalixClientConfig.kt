package com.mnemos.accountservice.integration.cristalix.config

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(CristalixApiProperties::class)
class CristalixClientConfig {

    @Bean
    @Qualifier("cristalixRestClient")
    fun cristalixRestClient(properties: CristalixApiProperties): RestClient {
        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(properties.connectTimeout)
            setReadTimeout(properties.readTimeout)
        }

        return RestClient.builder()
            .baseUrl(properties.url)
            .requestFactory(requestFactory)
            .defaultHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${properties.projectToken}"
            )
            .defaultHeader("X-Project-Key", properties.projectKey)
            .build()
    }

    @Bean
    @Qualifier("cristalixRateLimiter")
    fun cristalixRateLimiter(properties: CristalixApiProperties): RateLimiter {
        val config = RateLimiterConfig.custom()
            .limitForPeriod(properties.rateLimit.permits)
            .limitRefreshPeriod(properties.rateLimit.refreshPeriod)
            .timeoutDuration(properties.rateLimit.timeout)
            .build()

        return RateLimiter.of("cristalix-api", config)
    }
}