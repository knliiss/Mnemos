package com.mnemos.notification.application.booster.usecase.process

import com.mnemos.notification.application.booster.matcher.BoosterSubscriptionMatcher
import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import org.springframework.stereotype.Service

@Service
class ProcessBoosterEventService(
    private val subscriptionMatcher: BoosterSubscriptionMatcher,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessBoosterEventUseCase {
    override fun execute(command: ProcessBoosterEventCommand) {
        val recipientIds = subscriptionMatcher.match(command.boosterType)

        notificationDispatcher.dispatch(
            sourceId = command.messageId,
            recipientIds = recipientIds,
            payload =
                BoosterNotificationPayload(
                    boosterType = command.boosterType,
                    activatedBy = command.activatedBy,
                ),
            occurredAt = command.occurredAt,
        )
    }
}
