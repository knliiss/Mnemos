package com.mnemos.notification.application.notification.policy

import com.mnemos.notification.application.notification.delivery.model.DeliveryPriority
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import java.util.concurrent.atomic.AtomicLong

class DeliverySchedulingPolicy(
    eventWeights: Map<EventPriority, Int>,
    recipientWeights: Map<RecipientPriority, Int>,
) {
    private val eventCycle = weightedCycle(EventPriority.entries, eventWeights)
    private val recipientCycle = weightedCycle(RecipientPriority.entries, recipientWeights)
    private val counter = AtomicLong()

    fun nextOrder(): List<DeliveryPriority> {
        val index = counter.getAndIncrement()
        val preferredEvent = eventCycle[Math.floorMod(index, eventCycle.size.toLong()).toInt()]
        val preferredRecipient = recipientCycle[Math.floorMod(index, recipientCycle.size.toLong()).toInt()]

        val events = listOf(preferredEvent) + EVENT_ORDER.filterNot { it == preferredEvent }
        val recipients = listOf(preferredRecipient) + RECIPIENT_ORDER.filterNot { it == preferredRecipient }

        return buildList {
            events.forEach { event ->
                recipients.forEach { recipient ->
                    add(DeliveryPriority(event, recipient))
                }
            }
        }
    }

    private fun <T : Enum<T>> weightedCycle(
        values: List<T>,
        weights: Map<T, Int>,
    ): List<T> =
        buildList {
            values.forEach { value ->
                val weight = requireNotNull(weights[value])
                require(weight > 0)
                repeat(weight) { add(value) }
            }
        }

    private companion object {
        val EVENT_ORDER = listOf(EventPriority.CRITICAL, EventPriority.HIGH, EventPriority.NORMAL, EventPriority.LOW)
        val RECIPIENT_ORDER = listOf(RecipientPriority.PRIORITY, RecipientPriority.ELEVATED, RecipientPriority.STANDARD)
    }
}
