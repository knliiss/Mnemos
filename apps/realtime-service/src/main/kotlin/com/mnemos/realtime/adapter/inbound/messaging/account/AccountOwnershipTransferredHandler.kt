package com.mnemos.realtime.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountOwnershipTransferredV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.delivery.model.AccountOwnershipTransferredPayload
import org.springframework.stereotype.Component

@Component
class AccountOwnershipTransferredHandler(
    private val publisher: AccountRealtimeEventPublisher,
) : KafkaMessageHandler<AccountOwnershipTransferredV1> {
    override val definition =
        kafkaMessageDefinition<AccountOwnershipTransferredV1>(
            AccountEventContract.TOPIC,
            AccountEventType.ACCOUNT_OWNER_TRANSFERRED,
            AccountEventContract.VERSION,
            AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != ACCOUNT_SERVICE) {
                throw KafkaMessageProtocolException("Account lifecycle events must be produced by account-service")
            }
            if (envelope.payload.previousOwnerMemberId == envelope.payload.newOwnerMemberId) {
                throw KafkaMessageProtocolException("Account ownership transfer must change the owner member")
            }
        }

    override val consumer = "realtime-service.account-owner-transferred"

    override fun handle(message: ConsumedMessage<AccountOwnershipTransferredV1>) {
        val event = message.payload

        publisher.publish(
            message = message,
            recipientUserIds = event.recipientUserIds,
            payload =
                AccountOwnershipTransferredPayload(
                    accountId = message.aggregateId,
                    previousOwnerMemberId = event.previousOwnerMemberId,
                    previousOwnerUserId = event.previousOwnerUserId,
                    newOwnerMemberId = event.newOwnerMemberId,
                    newOwnerUserId = event.newOwnerUserId,
                ),
        )
    }

    private companion object {
        const val ACCOUNT_SERVICE = "account-service"
    }
}
