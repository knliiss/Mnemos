package com.mnemos.notification.configuration.notification

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.notification.delivery.worker")
data class NotificationDeliveryWorkerProperties(
    val interval: Duration = Duration.ofMillis(50),
    val recoveryInterval: Duration = Duration.ofSeconds(10),
    val batchSize: Int = 4,
    val recoveryBatchSize: Int = 100,
    val retryDelay: Duration = Duration.ofSeconds(1),
    val criticalWeight: Int = 8,
    val highWeight: Int = 4,
    val normalWeight: Int = 2,
    val lowWeight: Int = 1,
    val priorityRecipientWeight: Int = 4,
    val elevatedRecipientWeight: Int = 2,
    val standardRecipientWeight: Int = 1,
) {
    init {
        require(!interval.isZero && !interval.isNegative)
        require(!recoveryInterval.isZero && !recoveryInterval.isNegative)
        require(batchSize > 0)
        require(recoveryBatchSize > 0)
        require(!retryDelay.isZero && !retryDelay.isNegative)
        require(criticalWeight > 0 && highWeight > 0 && normalWeight > 0 && lowWeight > 0)
        require(priorityRecipientWeight > 0 && elevatedRecipientWeight > 0 && standardRecipientWeight > 0)
    }
}
