package com.mnemos.profile.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.profile.application.profile.usecase.projectaccount.ProjectAccountCreatedUseCase
import org.springframework.stereotype.Component

@Component
class AccountCreatedHandler(
    private val projectAccountCreatedUseCase: ProjectAccountCreatedUseCase,
) : KafkaMessageHandler<AccountCreatedV1> {
    override val definition =
        kafkaMessageDefinition<AccountCreatedV1>(
            AccountEventContract.TOPIC,
            AccountEventType.ACCOUNT_CREATED,
            AccountEventContract.VERSION,
            AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != ACCOUNT_SERVICE) {
                throw KafkaMessageProtocolException("Account creation must be produced by account-service")
            }
        }

    override val consumer = "profile-service.account-created"

    override fun handle(message: ConsumedMessage<AccountCreatedV1>) {
        val event = message.payload

        projectAccountCreatedUseCase.execute(
            accountId = message.aggregateId,
            cristalixUuid = event.cristalixUuid,
            cristalixNickname = event.cristalixNickname,
            occurredAt = message.occurredAt,
        )
    }

    private companion object {
        const val ACCOUNT_SERVICE = "account-service"
    }
}
