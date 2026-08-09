package com.mnemos.messaging.core.publisher

import com.mnemos.messaging.core.event.IntegrationEvent

fun interface IntegrationEventRouteResolver {

    fun resolve(
        event: IntegrationEvent<*>,
    ): IntegrationEventRoute
}