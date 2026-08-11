package com.mnemos.notification.adapter.inbound.scheduling.notification

import com.mnemos.notification.application.notification.service.NotificationDeliveryService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class TelegramDeliveryScheduler(
    private val deliveryService: NotificationDeliveryService,
) {
    @Scheduled(fixedDelayString = "\${mnemos.notification.delivery.worker.interval}")
    fun deliver() {
        deliveryService.deliver()
    }

    @Scheduled(fixedDelayString = "\${mnemos.notification.delivery.worker.recovery-interval}")
    fun recover() {
        deliveryService.recover()
    }
}
