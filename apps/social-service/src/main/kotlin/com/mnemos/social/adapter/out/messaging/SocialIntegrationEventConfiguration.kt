package com.mnemos.social.adapter.out.messaging

import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.social.contracts.SocialEventContract
import com.mnemos.social.contracts.SocialEventType
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SocialIntegrationEventConfiguration {
    @Bean
    fun socialIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                SocialEventType.DROP_SPOTLIGHT_PUBLISHED,
                SocialEventType.ACCOUNT_SOCIAL_SUMMARY_CHANGED ->
                    IntegrationEventRoute(
                        topic = SocialEventContract.TOPIC,
                        version = SocialEventContract.VERSION,
                        aggregateType = SocialEventContract.AGGREGATE_TYPE,
                    )

                WalletCommandType.CREATE_TRANSACTION ->
                    IntegrationEventRoute(
                        topic = WalletCommandContract.TOPIC,
                        version = WalletCommandContract.VERSION,
                        aggregateType = WalletCommandContract.AGGREGATE_TYPE,
                    )

                else -> error("Unsupported social integration event type: ${event.type}")
            }
        }
}
