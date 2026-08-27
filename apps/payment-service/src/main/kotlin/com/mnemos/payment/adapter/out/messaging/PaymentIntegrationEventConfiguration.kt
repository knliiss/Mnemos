package com.mnemos.payment.adapter.out.messaging

import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class PaymentIntegrationEventConfiguration {
    @Bean
    fun paymentIntegrationEventRouteResolver(): IntegrationEventRouteResolver =
        IntegrationEventRouteResolver { event ->
            when (event.type) {
                WalletCommandType.CREATE_TRANSACTION ->
                    IntegrationEventRoute(
                        topic = WalletCommandContract.TOPIC,
                        version = WalletCommandContract.VERSION,
                        aggregateType = WalletCommandContract.AGGREGATE_TYPE,
                    )

                else -> error("Unsupported payment integration event type: ${event.type}")
            }
        }
}
