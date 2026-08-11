package com.mnemos.account.infrastructure.messaging.config

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class AccountIntegrationEventConfiguration {
    @Bean
    fun accountIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                AccountEventType.ACCOUNT_CREATED ->
                    IntegrationEventRoute(
                        topic = AccountEventContract.TOPIC,
                        version = AccountEventContract.VERSION,
                        aggregateType = AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
                    )
                AccountEventType.USER_CREATED ->
                    IntegrationEventRoute(
                        topic = AccountEventContract.TOPIC,
                        version = AccountEventContract.VERSION,
                        aggregateType = AccountEventAggregateType.USER_AGGREGATE_TYPE,
                    )

                else -> error("Unsupported account-service integration event type: ${event.type}")
            }
        }
}
