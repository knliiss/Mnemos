package com.mnemos.notification.configuration.notification

import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.policy.DeliverySchedulingPolicy
import com.mnemos.notification.application.notification.policy.EventPriority
import com.mnemos.notification.application.notification.policy.NotificationExpirationPolicy
import com.mnemos.notification.application.notification.policy.NotificationPolicyRegistry
import com.mnemos.notification.application.notification.policy.NotificationTypePolicy
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableConfigurationProperties(
    NotificationDeliveryProperties::class,
    NotificationPolicyProperties::class,
    NotificationProcessingProperties::class,
    NotificationDeliveryWorkerProperties::class,
    MessagePackProperties::class,
)
class NotificationDeliveryConfiguration {
    @Bean
    fun notificationClock(): Clock = Clock.systemUTC()

    @Bean
    fun notificationExpirationPolicy(properties: NotificationDeliveryProperties): NotificationExpirationPolicy =
        NotificationExpirationPolicy(properties.ttl)

    @Bean
    fun notificationPolicyRegistry(properties: NotificationPolicyProperties): NotificationPolicyRegistry =
        NotificationPolicyRegistry(
            mapOf(
                NotificationType.ITEM_DROP to properties.itemDrop.toPolicy(),
                NotificationType.BOOSTER to properties.booster.toPolicy(),
                NotificationType.DUNGEON to properties.dungeon.toPolicy(),
                NotificationType.RAID to properties.raid.toPolicy(),
                NotificationType.GLOBAL to properties.global.toPolicy(),
            ),
        )

    @Bean
    fun deliverySchedulingPolicy(properties: NotificationDeliveryWorkerProperties): DeliverySchedulingPolicy =
        DeliverySchedulingPolicy(
            eventWeights =
                mapOf(
                    EventPriority.CRITICAL to properties.criticalWeight,
                    EventPriority.HIGH to properties.highWeight,
                    EventPriority.NORMAL to properties.normalWeight,
                    EventPriority.LOW to properties.lowWeight,
                ),
            recipientWeights =
                mapOf(
                    RecipientPriority.PRIORITY to properties.priorityRecipientWeight,
                    RecipientPriority.ELEVATED to properties.elevatedRecipientWeight,
                    RecipientPriority.STANDARD to properties.standardRecipientWeight,
                ),
        )

    private fun NotificationPolicyProperties.TypePolicy.toPolicy() =
        NotificationTypePolicy(
            aggregationEnabled = aggregationEnabled,
            aggregationWindow = aggregationWindow,
            priority = priority,
        )
}
