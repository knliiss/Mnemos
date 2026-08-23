package com.mnemos.wallet.adapter.inbound.messaging.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.wallet.application.balance.usecase.initialize.InitializeWalletBalancesService
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import org.springframework.stereotype.Component

@Component
class AccountCreatedWalletHandler(
    private val initializeWalletBalancesService: InitializeWalletBalancesService,
) : KafkaMessageHandler<AccountCreatedV1> {
    override val definition: KafkaMessageDefinition<AccountCreatedV1> =
        kafkaMessageDefinition(
            topic = AccountEventContract.TOPIC,
            type = AccountEventType.ACCOUNT_CREATED,
            version = AccountEventContract.VERSION,
            aggregateType = AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != ACCOUNT_SERVICE) {
                throw KafkaMessageProtocolException(
                    "Account creation must be produced by account-service",
                )
            }
        }

    override val consumer = "wallet-service.account-created"

    override fun handle(message: ConsumedMessage<AccountCreatedV1>) {
        initializeWalletBalancesService.execute(
            owner = WalletOwner.Account(AccountId(message.aggregateId)),
            occurredAt = message.occurredAt,
        )
    }

    private companion object {
        const val ACCOUNT_SERVICE = "account-service"
    }
}
