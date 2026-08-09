package com.mnemos.account.infrastructure.messaging.config

import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class AccountIntegrationEventConfiguration {
    @Bean
    fun accountIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver {
            IntegrationEventRoute(
                topic = AccountEventContract.TOPIC,
                version = AccountEventContract.VERSION,
                aggregateType =
                    AccountEventContract.AGGREGATE_TYPE,
            )
        }
}
