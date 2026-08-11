package com.mnemos.notification.configuration.notification

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.notification.processing")
data class NotificationProcessingProperties(
    val dispatchInterval: Duration = Duration.ofMillis(250),
    val recoveryInterval: Duration = Duration.ofSeconds(10),
    val claimBatchSize: Int = 100,
    val recoveryBatchSize: Int = 100,
    val recipientRetryDelay: Duration = Duration.ofSeconds(5),
    val processingRetryDelay: Duration = Duration.ofSeconds(1),
) {
    init {
        require(!dispatchInterval.isZero && !dispatchInterval.isNegative)
        require(!recoveryInterval.isZero && !recoveryInterval.isNegative)
        require(claimBatchSize > 0)
        require(recoveryBatchSize > 0)
        require(!recipientRetryDelay.isZero && !recipientRetryDelay.isNegative)
        require(!processingRetryDelay.isZero && !processingRetryDelay.isNegative)
    }
}
