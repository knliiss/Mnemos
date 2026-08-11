package com.mnemos.notification.configuration.redis

import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidateId
import com.mnemos.notification.application.notification.delivery.model.DeliveryPriority
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.notification.redis.delivery")
data class NotificationDeliveryQueueRedisProperties(
    val candidateKeyPrefix: String = "notification:{delivery}:candidate:",
    val laneKeyPrefix: String = "notification:{delivery}:lane:",
    val processingKeyPrefix: String = "notification:{delivery}:processing:",
    val processingDueKey: String = "notification:{delivery}:processing-due",
    val candidateTtl: Duration = Duration.ofHours(24),
    val processingTtl: Duration = Duration.ofHours(1),
    val visibilityTimeout: Duration = Duration.ofMinutes(1),
) {
    init {
        require(candidateKeyPrefix.isNotBlank())
        require(laneKeyPrefix.isNotBlank())
        require(processingKeyPrefix.isNotBlank())
        require(processingDueKey.isNotBlank())
        require(!candidateTtl.isZero && !candidateTtl.isNegative)
        require(!processingTtl.isZero && !processingTtl.isNegative)
        require(!visibilityTimeout.isZero && !visibilityTimeout.isNegative)
    }

    fun candidateKey(id: DeliveryCandidateId): String = "$candidateKeyPrefix${id.id}"

    fun processingKey(id: DeliveryCandidateId): String = "$processingKeyPrefix${id.id}"

    fun laneKey(priority: DeliveryPriority): String = "$laneKeyPrefix${priority.event.name}:${priority.recipient.name}"
}
