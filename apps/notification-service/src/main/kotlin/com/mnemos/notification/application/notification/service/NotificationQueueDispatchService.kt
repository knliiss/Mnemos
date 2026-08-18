package com.mnemos.notification.application.notification.service

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.aggregation.port.out.NotificationAggregationStore
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidate
import com.mnemos.notification.application.notification.delivery.model.DeliveryCandidateId
import com.mnemos.notification.application.notification.delivery.model.DeliveryPriority
import com.mnemos.notification.application.notification.immediate.port.out.ImmediateNotificationQueue
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.policy.EventPriority
import com.mnemos.notification.application.notification.policy.NotificationPolicyRegistry
import com.mnemos.notification.application.notification.service.NotificationBatchMapper.toItem
import com.mnemos.notification.application.recipient.model.RecipientView
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import com.mnemos.notification.configuration.notification.NotificationProcessingProperties
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class NotificationQueueDispatchService(
    private val aggregationStore: NotificationAggregationStore,
    private val immediateQueue: ImmediateNotificationQueue,
    private val recipientReader: RecipientReader,
    private val policyRegistry: NotificationPolicyRegistry,
    private val properties: NotificationProcessingProperties,
    private val clock: Clock,
) {
    fun dispatch() {
        dispatchAggregated()
        dispatchImmediate()
    }

    fun recover() {
        val now = clock.instant()
        aggregationStore.recoverExpired(now, properties.recoveryBatchSize)
        immediateQueue.recoverExpired(now, properties.recoveryBatchSize)
    }

    private fun dispatchAggregated() {
        val now = clock.instant()
        val claims = aggregationStore.claimDue(now, properties.claimBatchSize)
        if (claims.isEmpty()) return

        val activeItems =
            claims.associateWith { claim ->
                claim.items.filter { it.expiresAt.isAfter(now) }
            }

        activeItems
            .filterValues { it.isEmpty() }
            .keys
            .forEach(aggregationStore::complete)

        val deliverableClaims = activeItems.filterValues { it.isNotEmpty() }
        if (deliverableClaims.isEmpty()) return

        val recipients = recipientReader.findByUserIds(deliverableClaims.keys.mapTo(HashSet()) { it.userId })

        deliverableClaims.forEach { (claim, items) ->
            val recipient = recipients[claim.userId]
            if (recipient == null) {
                aggregationStore.release(claim, now.plus(properties.recipientRetryDelay))
                return@forEach
            }

            val candidate = createCandidate(claim.userId, recipient, items, now)

            try {
                aggregationStore.handoff(claim, candidate)
            } catch (_: Exception) {
                aggregationStore.release(claim, now.plus(properties.processingRetryDelay))
            }
        }
    }

    private fun dispatchImmediate() {
        val now = clock.instant()
        val claims = immediateQueue.claimDue(now, properties.claimBatchSize)
        if (claims.isEmpty()) return

        val activeClaims =
            claims.filter { claim ->
                if (claim.batch.expiresAt.isAfter(now)) {
                    true
                } else {
                    immediateQueue.complete(claim)
                    false
                }
            }

        if (activeClaims.isEmpty()) return

        val recipients =
            recipientReader.findByUserIds(
                activeClaims
                    .flatMapTo(HashSet()) { it.batch.recipientIds },
            )

        activeClaims.forEach { claim ->
            if (!recipients.keys.containsAll(claim.batch.recipientIds)) {
                immediateQueue.release(claim, now.plus(properties.recipientRetryDelay))
                return@forEach
            }

            val item = claim.batch.toItem()
            val candidates =
                claim.batch.recipientIds.map { userId ->
                    createCandidate(
                        userId = userId,
                        recipient = requireNotNull(recipients[userId]),
                        items = listOf(item),
                        readyAt = now,
                    )
                }

            try {
                immediateQueue.handoff(claim, candidates)
            } catch (_: Exception) {
                immediateQueue.release(claim, now.plus(properties.processingRetryDelay))
            }
        }
    }

    private fun createCandidate(
        userId: UserId,
        recipient: RecipientView,
        items: List<NotificationItem>,
        readyAt: Instant,
    ): DeliveryCandidate =
        DeliveryCandidate(
            id = DeliveryCandidateId.generate(userId, items.map { it.sourceId }),
            recipient = recipient,
            items = items,
            priority =
                DeliveryPriority(
                    event = highestPriority(items),
                    recipient = recipient.priority,
                ),
            readyAt = readyAt,
        )

    private fun highestPriority(items: List<NotificationItem>): EventPriority = items.maxOf { policyRegistry.get(it.payload.type).priority }
}
