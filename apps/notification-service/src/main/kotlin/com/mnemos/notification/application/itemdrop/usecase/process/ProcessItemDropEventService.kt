package com.mnemos.notification.application.itemdrop.usecase.process

import com.mnemos.notification.application.itemdrop.matcher.ItemDropSubscriptionMatcher
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import org.springframework.stereotype.Service

@Service
class ProcessItemDropEventService(
    private val subscriptionMatcher: ItemDropSubscriptionMatcher,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessItemDropEventUseCase {
    override fun execute(command: ProcessItemDropEventCommand) {
        val recipientIds =
            subscriptionMatcher.match(
                droppedFor = command.droppedFor,
                itemName = command.itemName,
            )

        notificationDispatcher.dispatch(
            sourceId = command.messageId,
            recipientIds = recipientIds,
            payload =
                ItemDropNotificationPayload(
                    droppedFor = command.droppedFor,
                    itemKey = command.itemKey,
                    itemName = command.itemName,
                    itemRarity = command.itemRarity,
                    itemType = command.itemType,
                ),
            occurredAt = command.occurredAt,
        )
    }
}
