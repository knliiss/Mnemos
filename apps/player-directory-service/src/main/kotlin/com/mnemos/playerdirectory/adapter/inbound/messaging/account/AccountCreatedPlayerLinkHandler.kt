package com.mnemos.playerdirectory.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.playerdirectory.application.accountlink.usecase.project.ProjectPlayerAccountLinkCommand
import com.mnemos.playerdirectory.application.accountlink.usecase.project.ProjectPlayerAccountLinkUseCase
import org.springframework.stereotype.Component

@Component
class AccountCreatedPlayerLinkHandler(
    private val useCase: ProjectPlayerAccountLinkUseCase,
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

    override val consumer = "player-directory-service.account-created-player-link"

    override fun handle(message: ConsumedMessage<AccountCreatedV1>) {
        useCase.execute(
            ProjectPlayerAccountLinkCommand(
                playerId = message.payload.cristalixUuid,
                accountId = message.aggregateId,
                recordedAt = message.occurredAt,
            ),
        )
    }

    private companion object {
        const val ACCOUNT_SERVICE = "account-service"
    }
}
