package com.mnemos.notification.application.notification.service

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.aggregation.port.out.NotificationAggregationStore
import com.mnemos.notification.application.notification.immediate.port.out.ImmediateNotificationQueue
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.notification.policy.EventPriority
import com.mnemos.notification.application.notification.policy.NotificationExpirationPolicy
import com.mnemos.notification.application.notification.policy.NotificationPolicyRegistry
import com.mnemos.notification.application.notification.policy.NotificationTypePolicy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class NotificationDispatcherTest {
    private val aggregationStore = mock<NotificationAggregationStore>()
    private val immediateQueue = mock<ImmediateNotificationQueue>()
    private val now = Instant.parse("2026-08-11T16:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `does nothing without recipients`() {
        val dispatcher = dispatcher(aggregationEnabled = true)

        dispatcher.dispatch(
            sourceId = UUID.randomUUID(),
            recipientIds = emptySet(),
            payload = RaidNotificationPayload(setOf(1)),
            occurredAt = now,
        )

        verifyNoInteractions(aggregationStore, immediateQueue)
    }

    @Test
    fun `aggregatable notification goes directly to aggregation store`() {
        val dispatcher = dispatcher(aggregationEnabled = true)
        val sourceId = UUID.randomUUID()

        dispatcher.dispatch(
            sourceId = sourceId,
            recipientIds = setOf(UserId(UUID.randomUUID())),
            payload = RaidNotificationPayload(setOf(1, 5)),
            occurredAt = now,
        )

        verify(aggregationStore).add(any(), eq(now.plusSeconds(10)))
        verifyNoInteractions(immediateQueue)
    }

    @Test
    fun `non aggregatable notification goes directly to immediate queue`() {
        val dispatcher = dispatcher(aggregationEnabled = false)

        dispatcher.dispatch(
            sourceId = UUID.randomUUID(),
            recipientIds = setOf(UserId(UUID.randomUUID())),
            payload = RaidNotificationPayload(setOf(1, 5)),
            occurredAt = now,
        )

        verify(immediateQueue).enqueue(any(), eq(now))
        verify(aggregationStore, never()).add(any(), any())
    }

    private fun dispatcher(aggregationEnabled: Boolean): NotificationDispatcher {
        val policy =
            NotificationTypePolicy(
                aggregationEnabled = aggregationEnabled,
                aggregationWindow = if (aggregationEnabled) Duration.ofSeconds(10) else null,
                priority = EventPriority.CRITICAL,
            )

        return NotificationDispatcher(
            aggregationStore = aggregationStore,
            immediateQueue = immediateQueue,
            expirationPolicy = NotificationExpirationPolicy(Duration.ofMinutes(5)),
            policyRegistry =
                NotificationPolicyRegistry(
                    NotificationType.entries.associateWith {
                        if (it == NotificationType.RAID) policy else NotificationTypePolicy(false, null, EventPriority.NORMAL)
                    },
                ),
            clock = clock,
        )
    }
}
