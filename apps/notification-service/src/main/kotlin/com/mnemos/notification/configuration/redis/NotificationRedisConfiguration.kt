package com.mnemos.notification.configuration.redis

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.RedisScript

@Configuration
@EnableConfigurationProperties(
    NotificationAggregationRedisProperties::class,
    ImmediateNotificationRedisProperties::class,
    NotificationDeliveryQueueRedisProperties::class,
)
class NotificationRedisConfiguration {
    @Bean
    fun addAggregationScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/add-aggregation.lua"), Long::class.javaObjectType)

    @Bean
    fun claimAggregationScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/claim-aggregation.lua"), Long::class.javaObjectType)

    @Bean
    fun releaseAggregationScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/release-aggregation.lua"), Long::class.javaObjectType)

    @Bean
    fun completeProcessingScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/complete-processing.lua"), Long::class.javaObjectType)

    @Bean
    fun enqueueImmediateScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/enqueue-immediate.lua"), Long::class.javaObjectType)

    @Bean
    fun claimJsonQueueScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/claim-json-queue.lua"), Long::class.javaObjectType)

    @Bean
    fun releaseJsonQueueScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/release-json-queue.lua"), Long::class.javaObjectType)

    @Bean
    fun handoffAggregationScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/handoff-aggregation.lua"), Long::class.javaObjectType)

    @Bean
    fun handoffImmediateScript(): RedisScript<Long> =
        RedisScript.of(ClassPathResource("redis/handoff-immediate.lua"), Long::class.javaObjectType)
}
