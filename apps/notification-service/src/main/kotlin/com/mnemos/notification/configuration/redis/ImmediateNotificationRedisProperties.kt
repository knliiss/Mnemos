package com.mnemos.notification.configuration.redis

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import java.util.UUID

@ConfigurationProperties("mnemos.notification.redis.immediate")
data class ImmediateNotificationRedisProperties(
    val readyKey: String = "notification:{delivery}:immediate:ready",
    val acceptedKeyPrefix: String = "notification:{delivery}:immediate:accepted:",
    val batchKeyPrefix: String = "notification:{delivery}:immediate:batch:",
    val processingKeyPrefix: String = "notification:{delivery}:immediate:processing:",
    val processingDueKey: String = "notification:{delivery}:immediate:processing-due",
    val acceptedTtl: Duration = Duration.ofHours(24),
    val batchTtl: Duration = Duration.ofHours(1),
    val processingTtl: Duration = Duration.ofHours(1),
    val visibilityTimeout: Duration = Duration.ofMinutes(1),
) {
    init {
        require(readyKey.isNotBlank())
        require(acceptedKeyPrefix.isNotBlank())
        require(batchKeyPrefix.isNotBlank())
        require(processingKeyPrefix.isNotBlank())
        require(processingDueKey.isNotBlank())
        require(!acceptedTtl.isZero && !acceptedTtl.isNegative)
        require(!batchTtl.isZero && !batchTtl.isNegative)
        require(!processingTtl.isZero && !processingTtl.isNegative)
        require(!visibilityTimeout.isZero && !visibilityTimeout.isNegative)
    }

    fun acceptedKey(sourceId: UUID): String = "$acceptedKeyPrefix$sourceId"

    fun batchKey(sourceId: UUID): String = "$batchKeyPrefix$sourceId"

    fun processingKey(sourceId: UUID): String = "$processingKeyPrefix$sourceId"
}
