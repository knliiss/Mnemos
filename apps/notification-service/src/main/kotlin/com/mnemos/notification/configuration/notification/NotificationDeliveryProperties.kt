package com.mnemos.notification.configuration.notification

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.notification.delivery")
data class NotificationDeliveryProperties(
    val ttl: Duration = Duration.ofMinutes(5),
) {
    init {
        require(!ttl.isZero && !ttl.isNegative)
    }
}
