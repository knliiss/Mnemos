package com.mnemos.moderation.adapter.out.messaging

import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.moderation.contracts.ModerationEventContract
import com.mnemos.moderation.contracts.ModerationEventType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class ModerationIntegrationEventConfiguration {
    @Bean
    fun moderationIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                ModerationEventType.SANCTION_ACTIVATED,
                ModerationEventType.SANCTION_DEACTIVATED,
                ->
                    IntegrationEventRoute(
                        topic = ModerationEventContract.TOPIC,
                        version = ModerationEventContract.VERSION,
                        aggregateType = ModerationEventContract.AGGREGATE_TYPE,
                    )

                else -> error("Unsupported moderation event type: ${event.type}")
            }
        }
}
