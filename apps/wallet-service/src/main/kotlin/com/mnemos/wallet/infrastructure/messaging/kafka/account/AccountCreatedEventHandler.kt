package com.mnemos.wallet.infrastructure.messaging.kafka.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.wallet.application.balance.InitializeAccountBalancesService
import org.springframework.stereotype.Component

@Component
class AccountCreatedEventHandler(
    private val initializeAccountBalancesService: InitializeAccountBalancesService,
) : KafkaMessageHandler<AccountCreatedV1> {
    override val definition: KafkaMessageDefinition<AccountCreatedV1> =
        kafkaMessageDefinition(
            topic = AccountEventContract.TOPIC,
            type = AccountEventType.ACCOUNT_CREATED,
            version = AccountEventContract.VERSION,
            aggregateType = AccountEventContract.AGGREGATE_TYPE,
        )

    override val consumer = "wallet-service.account-created"

    override fun handle(message: ConsumedMessage<AccountCreatedV1>) {
        initializeAccountBalancesService.initialize(AccountId(message.aggregateId))
    }
}
