package com.mnemos.notification.application.notification.service

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.aggregation.port.out.NotificationAggregationStore
import com.mnemos.notification.application.notification.immediate.port.out.ImmediateNotificationQueue
import com.mnemos.notification.application.notification.model.NotificationBatch
import com.mnemos.notification.application.notification.model.NotificationPayload
import com.mnemos.notification.application.notification.policy.NotificationExpirationPolicy
import com.mnemos.notification.application.notification.policy.NotificationPolicyRegistry
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class NotificationDispatcher(
    private val aggregationStore: NotificationAggregationStore,
    private val immediateQueue: ImmediateNotificationQueue,
    private val expirationPolicy: NotificationExpirationPolicy,
    private val policyRegistry: NotificationPolicyRegistry,
    private val clock: Clock,
) {
    fun dispatch(
        sourceId: UUID,
        recipientIds: Set<UserId>,
        payload: NotificationPayload,
        occurredAt: Instant,
    ) {
        if (recipientIds.isEmpty()) return

        val now = clock.instant()
        val batch =
            NotificationBatch(
                sourceId = sourceId,
                recipientIds = recipientIds,
                payload = payload,
                occurredAt = occurredAt,
                expiresAt = expirationPolicy.expiresAt(occurredAt),
            )

        if (!batch.expiresAt.isAfter(now)) return

        val policy = policyRegistry.get(payload.type)

        if (policy.aggregationEnabled) {
            aggregationStore.add(
                batch = batch,
                flushAt = now.plus(requireNotNull(policy.aggregationWindow)),
            )
        } else {
            immediateQueue.enqueue(
                batch = batch,
                availableAt = now,
            )
        }
    }
}
