package com.mnemos.personalization.adapter.out.messaging

import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PersonalizationIntegrationEventConfiguration {
    @Bean
    fun personalizationIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                PersonalizationEventType.PURCHASE_COMPLETED ->
                    IntegrationEventRoute(
                        topic = PersonalizationEventContract.TOPIC,
                        version = PersonalizationEventContract.VERSION,
                        aggregateType = PersonalizationAggregateType.PURCHASE_OPERATION,
                    )

                PersonalizationEventType.NOTIFICATION_PACK_SELECTION_CHANGED ->
                    IntegrationEventRoute(
                        topic = PersonalizationEventContract.TOPIC,
                        version = PersonalizationEventContract.VERSION,
                        aggregateType = PersonalizationAggregateType.USER_NOTIFICATION_PACK_SELECTION,
                    )

                PersonalizationEventType.PROFILE_APPEARANCE_CHANGED ->
                    IntegrationEventRoute(
                        topic = PersonalizationEventContract.TOPIC,
                        version = PersonalizationEventContract.VERSION,
                        aggregateType = PersonalizationAggregateType.ACCOUNT_PROFILE_APPEARANCE,
                    )

                WalletCommandType.CREATE_TRANSACTION ->
                    IntegrationEventRoute(
                        topic = WalletCommandContract.TOPIC,
                        version = WalletCommandContract.VERSION,
                        aggregateType = WalletCommandContract.AGGREGATE_TYPE,
                    )

                else -> error("Unsupported personalization integration event type: ${event.type}")
            }
        }
}
