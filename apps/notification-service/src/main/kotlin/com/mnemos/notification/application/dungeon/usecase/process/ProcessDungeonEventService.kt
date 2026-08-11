package com.mnemos.notification.application.dungeon.usecase.process

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.notification.application.dungeon.matcher.DungeonSubscriptionMatcher
import com.mnemos.notification.application.notification.model.DungeonNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ProcessDungeonEventService(
    private val subscriptionMatcher: DungeonSubscriptionMatcher,
    private val notificationDispatcher: NotificationDispatcher,
) : ProcessDungeonEventUseCase {
    override fun execute(command: ProcessDungeonEventCommand) {
        val recipientIds = subscriptionMatcher.match(command.dungeonType)
        val occurredAt = command.occurredAt.truncatedTo(ChronoUnit.MINUTES)

        notificationDispatcher.dispatch(
            sourceId = generateSourceId(command.dungeonType, occurredAt),
            recipientIds = recipientIds,
            payload = DungeonNotificationPayload(command.dungeonType),
            occurredAt = occurredAt,
        )
    }

    private fun generateSourceId(
        dungeonType: DomainDungeonType,
        occurredAt: Instant,
    ): UUID =
        UuidCreator.getNameBasedSha1(
            "mnemos:notification:dungeon:${dungeonType.name}:${occurredAt.epochSecond}",
        )
}
