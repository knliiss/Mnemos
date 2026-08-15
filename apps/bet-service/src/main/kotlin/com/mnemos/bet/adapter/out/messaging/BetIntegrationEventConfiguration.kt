package com.mnemos.bet.adapter.out.messaging

import com.mnemos.bet.contracts.BetAggregateType
import com.mnemos.bet.contracts.BetEventContract
import com.mnemos.bet.contracts.BetEventType
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class BetIntegrationEventConfiguration {
    @Bean
    fun betIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                BetEventType.EVENT_BET_OPENED,
                BetEventType.EVENT_BET_SETTLED,
                ->
                    IntegrationEventRoute(
                        topic = BetEventContract.TOPIC,
                        version = BetEventContract.VERSION,
                        aggregateType = BetAggregateType.EVENT_BET_ROUND,
                    )

                BetEventType.ITEM_RACE_OPENED,
                BetEventType.ITEM_RACE_SETTLED,
                ->
                    IntegrationEventRoute(
                        topic = BetEventContract.TOPIC,
                        version = BetEventContract.VERSION,
                        aggregateType = BetAggregateType.ITEM_RACE,
                    )

                BetEventType.OPERATION_COMPLETED ->
                    IntegrationEventRoute(
                        topic = BetEventContract.TOPIC,
                        version = BetEventContract.VERSION,
                        aggregateType = BetAggregateType.OPERATION,
                    )

                WalletCommandType.CREATE_TRANSACTION ->
                    IntegrationEventRoute(
                        topic = WalletCommandContract.TOPIC,
                        version = WalletCommandContract.VERSION,
                        aggregateType = WalletCommandContract.AGGREGATE_TYPE,
                    )

                else -> error("Unsupported bet integration event type: ${event.type}")
            }
        }
}
