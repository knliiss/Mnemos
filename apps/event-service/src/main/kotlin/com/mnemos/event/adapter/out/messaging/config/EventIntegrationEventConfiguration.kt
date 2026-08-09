package com.mnemos.event.adapter.out.messaging.config

import com.mnemos.event.contracts.EventAggregateType
import com.mnemos.event.contracts.EventEventContract
import com.mnemos.event.contracts.EventEventType
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class EventIntegrationEventConfiguration {
    @Bean
    fun eventIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                EventEventType.ITEM_DROPPED -> route(EventAggregateType.ITEM_DROP_EVENT)
                EventEventType.RAID_STARTED -> route(EventAggregateType.RAID_EVENT)
                EventEventType.BOOSTER_ACTIVATED -> route(EventAggregateType.BOOSTER_EVENT)
                EventEventType.GLOBAL_STARTED -> route(EventAggregateType.GLOBAL_EVENT)
                else -> error("Unsupported event-service integration event type: ${event.type}")
            }
        }

    private fun route(aggregateType: String): IntegrationEventRoute =
        IntegrationEventRoute(
            topic = EventEventContract.TOPIC,
            version = EventEventContract.VERSION,
            aggregateType = aggregateType,
        )
}
