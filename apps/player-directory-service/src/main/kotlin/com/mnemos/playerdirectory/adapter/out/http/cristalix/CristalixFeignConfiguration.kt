package com.mnemos.playerdirectory.adapter.out.http.cristalix

import feign.Client
import feign.Logger
import okhttp3.OkHttpClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(CristalixApiProperties::class)
class CristalixFeignConfiguration(
    private val properties: CristalixApiProperties,
) {
    @Bean
    fun feignLoggerLevel(): Logger.Level = Logger.Level.BASIC

    @Bean
    fun okHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(properties.connectTimeout.toMillis(), TimeUnit.MILLISECONDS)
            .readTimeout(properties.readTimeout.toMillis(), TimeUnit.MILLISECONDS)
            .build()

    @Bean
    fun feignClient(okHttpClient: OkHttpClient): Client = feign.okhttp.OkHttpClient(okHttpClient)
}
