package com.mnemos.account.integration.cristalix.config

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CristalixFeignConfig(
    private val properties: CristalixApiProperties,
) {
    @Bean
    fun feignLoggerLevel(): feign.Logger.Level = feign.Logger.Level.FULL

    @Configuration
    class FeignClientConfig {
        @Bean
        fun okHttpClient(): okhttp3.OkHttpClient =
            okhttp3.OkHttpClient
                .Builder()
                .build()

        @Bean
        fun feignClient(okHttpClient: okhttp3.OkHttpClient): feign.Client = feign.okhttp.OkHttpClient(okHttpClient)
    }

    @Bean
    fun inspectClient(client: feign.Client) =
        ApplicationRunner {
            println("Feign client: ${client.javaClass.name}")
        }
}
