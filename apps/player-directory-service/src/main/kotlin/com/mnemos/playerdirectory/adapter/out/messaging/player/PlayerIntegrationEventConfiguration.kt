package com.mnemos.playerdirectory.adapter.out.messaging.player

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventAggregateType
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventContract
import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PlayerIntegrationEventConfiguration {
    @Bean
    fun playerDirectoryIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                PlayerDirectoryEventType.PLAYER_OBSERVED,
                PlayerDirectoryEventType.PRESENCE_CHANGED,
                PlayerDirectoryEventType.WENT_OFFLINE,
                PlayerDirectoryEventType.NICKNAME_CHANGED,
                PlayerDirectoryEventType.PRESENCE_SUBSCRIPTION_CREATED,
                PlayerDirectoryEventType.PRESENCE_SUBSCRIPTION_DELETED,
                -> route()

                else -> error("Unsupported player-directory integration event type: ${event.type}")
            }
        }

    private fun route(): IntegrationEventRoute =
        IntegrationEventRoute(
            topic = PlayerDirectoryEventContract.TOPIC,
            version = PlayerDirectoryEventContract.VERSION,
            aggregateType = PlayerDirectoryEventAggregateType.PLAYER,
        )
}
