package com.mnemos.notification.application.raid.usecase.process

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.application.raid.matcher.RaidSubscriptionMatcher
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.UUID

@Service
class ProcessRaidEventService(
    private val subscriptionMatcher: RaidSubscriptionMatcher,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessRaidEventUseCase {
    override fun execute(command: ProcessRaidEventCommand) {
        val matchedLocationsByUser = subscriptionMatcher.match(command.locations)

        matchedLocationsByUser.forEach { (recipientId, matchedLocations) ->
            notificationDispatcher.dispatch(
                sourceId = personalizedSourceId(command.messageId, recipientId),
                recipientIds = setOf(recipientId),
                payload = RaidNotificationPayload(matchedLocations),
                occurredAt = command.occurredAt,
            )
        }
    }

    private fun personalizedSourceId(
        sourceId: UUID,
        recipientId: UserId,
    ): UUID =
        UUID.nameUUIDFromBytes(
            "$RAID_SOURCE_NAMESPACE:$sourceId:${recipientId.id}"
                .toByteArray(StandardCharsets.UTF_8),
        )

    private companion object {
        const val RAID_SOURCE_NAMESPACE = "raid-notification"
    }
}
