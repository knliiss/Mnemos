package com.mnemos.notification.application.raid.usecase.process

import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.application.raid.matcher.RaidSubscriptionMatcher
import org.springframework.stereotype.Service

@Service
class ProcessRaidEventService(
    private val subscriptionMatcher: RaidSubscriptionMatcher,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessRaidEventUseCase {
    override fun execute(command: ProcessRaidEventCommand) {
        val recipientIds = subscriptionMatcher.match(command.locations)

        notificationDispatcher.dispatch(
            sourceId = command.messageId,
            recipientIds = recipientIds,
            payload = RaidNotificationPayload(command.locations),
            occurredAt = command.occurredAt,
        )
    }
}
