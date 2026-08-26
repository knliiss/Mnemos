package com.mnemos.notification.application.playerpresence.usecase.process

import com.mnemos.notification.application.notification.model.PlayerOfflineNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.application.playerpresence.port.out.PlayerPresenceSubscriptionRepository
import org.springframework.stereotype.Service

@Service
class ProcessPlayerWentOfflineEventService(
    private val subscriptionRepository: PlayerPresenceSubscriptionRepository,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessPlayerWentOfflineEventUseCase {
    override fun execute(command: ProcessPlayerWentOfflineEventCommand) {
        val recipientIds = subscriptionRepository.findSubscriberUserIds(command.playerId)

        notificationDispatcher.dispatch(
            sourceId = command.messageId,
            recipientIds = recipientIds,
            payload =
                PlayerOfflineNotificationPayload(
                    playerId = command.playerId,
                    player = command.nickname,
                    previousRealm = command.previousRealm,
                    lastSeenOnline = command.lastSeenOnline,
                ),
            occurredAt = command.occurredAt,
        )
    }
}
