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
    fun accountIntegrationEventRouteResolver(): IntegrationEventRouteResolver {
        val accountRoute =
            IntegrationEventRoute(
                topic = AccountEventContract.TOPIC,
                version = AccountEventContract.VERSION,
                aggregateType = AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
            )
        val userRoute =
            IntegrationEventRoute(
                topic = AccountEventContract.TOPIC,
                version = AccountEventContract.VERSION,
                aggregateType = AccountEventAggregateType.USER_AGGREGATE_TYPE,
            )

        return IntegrationEventRouteResolver { event ->
            when (event.type) {
                AccountEventType.ACCOUNT_CREATED,
                AccountEventType.ACCOUNT_ACTIVATED,
                AccountEventType.ACCOUNT_DEACTIVATED,
                AccountEventType.ACCOUNT_DELETED,
                AccountEventType.ACCOUNT_OWNER_TRANSFERRED,
                AccountEventType.ACCOUNT_MEMBER_JOINED,
                AccountEventType.ACCOUNT_MEMBER_LEFT,
                AccountEventType.ACCOUNT_MEMBER_REMOVED,
                -> accountRoute

                AccountEventType.USER_CREATED -> userRoute

                else -> error("Unsupported account-service integration event type: ${event.type}")
            }
        }
    }
}
