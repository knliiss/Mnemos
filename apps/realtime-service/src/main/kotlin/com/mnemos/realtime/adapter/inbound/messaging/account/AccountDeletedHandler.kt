package com.mnemos.realtime.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountStatusChangedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.delivery.model.AccountStatusChangedPayload
import org.springframework.stereotype.Component

@Component
class AccountDeletedHandler(
    private val publisher: AccountRealtimeEventPublisher,
) : KafkaMessageHandler<AccountStatusChangedV1> {
    override val definition =
        kafkaMessageDefinition<AccountStatusChangedV1>(
            AccountEventContract.TOPIC,
            AccountEventType.ACCOUNT_DELETED,
            AccountEventContract.VERSION,
            AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != ACCOUNT_SERVICE) {
                throw KafkaMessageProtocolException("Account lifecycle events must be produced by account-service")
            }
            if (envelope.payload.status != "DELETED") {
                throw KafkaMessageProtocolException("Account deletion event must carry DELETED status")
            }
        }

    override val consumer = "realtime-service.account-deleted"

    override fun handle(message: ConsumedMessage<AccountStatusChangedV1>) {
        publisher.publish(
            message = message,
            recipientUserIds = message.payload.recipientUserIds,
            payload =
                AccountStatusChangedPayload(
                    accountId = message.aggregateId,
                    status = message.payload.status,
                ),
        )
    }

    private companion object {
        const val ACCOUNT_SERVICE = "account-service"
    }
}
