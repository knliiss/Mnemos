package com.mnemos.notification.configuration.redis

import com.mnemos.common.core.model.id.UserId
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import java.util.UUID

@ConfigurationProperties("mnemos.notification.redis.aggregation")
data class NotificationAggregationRedisProperties(
    val dueKey: String = "notification:{delivery}:aggregation:due",
    val acceptedKeyPrefix: String = "notification:{delivery}:aggregation:accepted:",
    val bucketKeyPrefix: String = "notification:{delivery}:aggregation:bucket:",
    val processingKeyPrefix: String = "notification:{delivery}:aggregation:processing:",
    val processingDueKey: String = "notification:{delivery}:aggregation:processing-due",
    val acceptedTtl: Duration = Duration.ofHours(24),
    val bucketTtl: Duration = Duration.ofMinutes(10),
    val processingTtl: Duration = Duration.ofHours(1),
    val visibilityTimeout: Duration = Duration.ofMinutes(1),
) {
    init {
        require(dueKey.isNotBlank())
        require(acceptedKeyPrefix.isNotBlank())
        require(bucketKeyPrefix.isNotBlank())
        require(processingKeyPrefix.isNotBlank())
        require(processingDueKey.isNotBlank())
        require(!acceptedTtl.isZero && !acceptedTtl.isNegative)
        require(!bucketTtl.isZero && !bucketTtl.isNegative)
        require(!processingTtl.isZero && !processingTtl.isNegative)
        require(!visibilityTimeout.isZero && !visibilityTimeout.isNegative)
    }

    fun acceptedKey(sourceId: UUID): String = "$acceptedKeyPrefix$sourceId"

    fun bucketKey(userId: UserId): String = "$bucketKeyPrefix${userId.id}"

    fun processingKey(
        userId: UserId,
        claimId: UUID,
    ): String = "$processingKeyPrefix${userId.id}:$claimId"

    fun processingUserId(processingKey: String): UserId {
        require(processingKey.startsWith(processingKeyPrefix))
        val raw = processingKey.removePrefix(processingKeyPrefix)
        return UserId(UUID.fromString(raw.substringBefore(':')))
    }
}
