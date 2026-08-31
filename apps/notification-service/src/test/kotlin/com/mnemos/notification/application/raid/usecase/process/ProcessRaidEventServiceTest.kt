package com.mnemos.notification.application.raid.usecase.process

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.application.raid.matcher.RaidSubscriptionMatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ProcessRaidEventServiceTest {
    private val subscriptionMatcher = mock<RaidSubscriptionMatcher>()
    private val notificationDispatcher = mock<NotificationDispatcher>()
    private val service =
        ProcessRaidEventService(
            subscriptionMatcher = subscriptionMatcher,
            notificationDispatcher = notificationDispatcher,
        )

    @Test
    fun `dispatches only matched raid locations to each recipient`() {
        val firstUser = UserId(UUID.randomUUID())
        val secondUser = UserId(UUID.randomUUID())
        val command = command(locations = setOf(1, 2, 5))

        whenever(subscriptionMatcher.match(command.locations))
            .thenReturn(
                linkedMapOf(
                    firstUser to setOf(2),
                    secondUser to setOf(2, 5),
                ),
            )

        service.execute(command)

        verify(notificationDispatcher).dispatch(
            sourceId = any(),
            recipientIds = eq(setOf(firstUser)),
            payload = eq(RaidNotificationPayload(setOf(2))),
            occurredAt = eq(command.occurredAt),
        )
        verify(notificationDispatcher).dispatch(
            sourceId = any(),
            recipientIds = eq(setOf(secondUser)),
            payload = eq(RaidNotificationPayload(setOf(2, 5))),
            occurredAt = eq(command.occurredAt),
        )
    }

    @Test
    fun `uses stable recipient specific source id for raid replay deduplication`() {
        val userId = UserId(UUID.randomUUID())
        val command = command(locations = setOf(3, 7))

        whenever(subscriptionMatcher.match(command.locations))
            .thenReturn(mapOf(userId to setOf(3)))

        service.execute(command)
        service.execute(command)

        val sourceIdCaptor = argumentCaptor<UUID>()

        verify(notificationDispatcher, times(2)).dispatch(
            sourceId = sourceIdCaptor.capture(),
            recipientIds = eq(setOf(userId)),
            payload = eq(RaidNotificationPayload(setOf(3))),
            occurredAt = eq(command.occurredAt),
        )

        assertEquals(sourceIdCaptor.firstValue, sourceIdCaptor.secondValue)
    }

    private fun command(locations: Set<Int>): ProcessRaidEventCommand =
        ProcessRaidEventCommand(
            messageId = UUID.randomUUID(),
            occurredAt = Instant.parse("2026-08-31T20:30:00Z"),
            locations = locations,
        )
}
