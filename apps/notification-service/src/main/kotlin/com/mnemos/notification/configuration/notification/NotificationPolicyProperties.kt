package com.mnemos.notification.configuration.notification

import com.mnemos.notification.application.notification.policy.EventPriority
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.notification.policy")
data class NotificationPolicyProperties(
    val itemDrop: TypePolicy,
    val booster: TypePolicy,
    val dungeon: TypePolicy,
    val raid: TypePolicy,
    val global: TypePolicy,
    val playerOffline: TypePolicy,
) {
    data class TypePolicy(
        val aggregationEnabled: Boolean,
        val aggregationWindow: Duration?,
        val priority: EventPriority,
    )
}
