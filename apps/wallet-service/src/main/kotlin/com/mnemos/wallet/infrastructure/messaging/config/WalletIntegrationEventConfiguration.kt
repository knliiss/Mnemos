package com.mnemos.wallet.infrastructure.messaging.kafka.config

import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class WalletIntegrationEventConfiguration {
    @Bean
    fun walletIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                WalletEventType.TRANSACTION_COMPLETED,
                WalletEventType.TRANSACTION_FAILED,
                ->
                    IntegrationEventRoute(
                        topic = WalletEventContract.TOPIC,
                        version = WalletEventContract.VERSION,
                        aggregateType = WalletEventContract.AGGREGATE_TYPE,
                    )

                else ->
                    error(
                        "Unsupported wallet integration event type: ${event.type}",
                    )
            }
        }
}
