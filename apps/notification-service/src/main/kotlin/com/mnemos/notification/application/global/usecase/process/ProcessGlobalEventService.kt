package com.mnemos.notification.application.global.usecase.process

import com.mnemos.notification.application.global.matcher.GlobalSubscriptionMatcher
import com.mnemos.notification.application.notification.model.GlobalNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import org.springframework.stereotype.Service

@Service
class ProcessGlobalEventService(
    private val subscriptionMatcher: GlobalSubscriptionMatcher,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessGlobalEventUseCase {
    override fun execute(command: ProcessGlobalEventCommand) {
        val recipientIds = subscriptionMatcher.match(command.eventType)

        notificationDispatcher.dispatch(
            sourceId = command.messageId,
            recipientIds = recipientIds,
            payload = GlobalNotificationPayload(command.eventType),
            occurredAt = command.occurredAt,
        )
    }
}
