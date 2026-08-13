package com.mnemos.notification.application.notification.service

import com.mnemos.notification.application.notification.delivery.model.ClaimedDeliveryCandidate
import com.mnemos.notification.application.notification.delivery.model.DeliveryResult
import com.mnemos.notification.application.notification.delivery.port.out.MessageDeliveryGateway
import com.mnemos.notification.application.notification.delivery.port.out.NotificationDeliveryQueue
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.notification.policy.DeliverySchedulingPolicy
import com.mnemos.notification.configuration.notification.NotificationDeliveryWorkerProperties
import com.mnemos.notification.domain.recipient.repository.RecipientRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Clock

@Service
@ConditionalOnProperty(prefix = "mnemos.notification.telegram", name = ["enabled"], havingValue = "true")
class NotificationDeliveryService(
    private val queue: NotificationDeliveryQueue,
    private val schedulingPolicy: DeliverySchedulingPolicy,
    private val renderer: NotificationMessageRenderer,
    private val gateway: MessageDeliveryGateway,
    private val recipientRepository: RecipientRepository,
    private val properties: NotificationDeliveryWorkerProperties,
    private val clock: Clock,
) {
    fun deliver() {
        repeat(properties.batchSize) {
            val now = clock.instant()
            val claim = claimNext(now) ?: return
            deliver(claim, now)
        }
    }

    fun recover() {
        queue.recoverExpired(clock.instant(), properties.recoveryBatchSize)
    }

    private fun claimNext(now: java.time.Instant): ClaimedDeliveryCandidate? {
        schedulingPolicy.nextOrder().forEach { priority ->
            queue.claim(priority, now)?.let { return it }
        }
        return null
    }

    private fun deliver(
        claim: ClaimedDeliveryCandidate,
        now: java.time.Instant,
    ) {
        val candidate = claim.candidate
        val activeItems = candidate.items.filter { it.expiresAt.isAfter(now) }

        if (activeItems.isEmpty()) {
            queue.complete(claim)
            return
        }

        try {
            val message = renderer.renderNotification(candidate.userId, activeItems)

            when (val result = gateway.deliver(candidate.recipient.telegramId, message)) {
                DeliveryResult.Delivered -> queue.complete(claim)

                is DeliveryResult.PermanentFailure -> {
                    if (result.reason == "TELEGRAM_BLOCKED") {
                        recipientRepository.blockByTelegramId(candidate.recipient.telegramId)
                    }

                    queue.complete(claim)
                }

                is DeliveryResult.Retryable ->
                    queue.release(
                        claim,
                        now.plus(result.retryAfter ?: properties.retryDelay),
                    )
            }
        } catch (_: Exception) {
            queue.release(claim, now.plus(properties.retryDelay))
        }
    }
}
