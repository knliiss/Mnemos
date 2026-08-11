package com.mnemos.notification.adapter.inbound.scheduling.notification

import com.mnemos.notification.application.notification.service.NotificationQueueDispatchService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.notification.processing", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class NotificationPipelineScheduler(
    private val dispatchService: NotificationQueueDispatchService,
) {
    @Scheduled(fixedDelayString = "\${mnemos.notification.processing.dispatch-interval}")
    fun dispatch() {
        dispatchService.dispatch()
    }

    @Scheduled(fixedDelayString = "\${mnemos.notification.processing.recovery-interval}")
    fun recover() {
        dispatchService.recover()
    }
}
