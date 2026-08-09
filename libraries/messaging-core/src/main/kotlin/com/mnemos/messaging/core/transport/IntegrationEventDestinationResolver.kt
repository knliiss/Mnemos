package com.mnemos.messaging.core.transport

import com.mnemos.messaging.core.event.IntegrationEvent

fun interface IntegrationEventDestinationResolver {

    fun resolve(
        event: IntegrationEvent<*>,
    ): EventDestination

    companion object {

        fun singleTopic(
            topic: String,
        ): IntegrationEventDestinationResolver =
            IntegrationEventDestinationResolver { event ->
                EventDestination(
                    topic = topic,
                    key = event.aggregateId.toString(),
                )
            }
    }
}