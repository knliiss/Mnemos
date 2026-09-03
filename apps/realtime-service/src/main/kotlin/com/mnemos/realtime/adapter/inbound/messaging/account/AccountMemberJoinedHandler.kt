package com.mnemos.realtime.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountMemberChangedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.realtime.application.delivery.model.AccountMemberChangedPayload
import org.springframework.stereotype.Component

@Component
class AccountMemberJoinedHandler(
    private val publisher: AccountRealtimeEventPublisher,
) : KafkaMessageHandler<AccountMemberChangedV1> {
    override val definition =
        kafkaMessageDefinition<AccountMemberChangedV1>(
            AccountEventContract.TOPIC,
            AccountEventType.ACCOUNT_MEMBER_JOINED,
            AccountEventContract.VERSION,
            AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != ACCOUNT_SERVICE) {
                throw KafkaMessageProtocolException("Account lifecycle events must be produced by account-service")
            }
        }

    override val consumer = "realtime-service.account-member-joined"

    override fun handle(message: ConsumedMessage<AccountMemberChangedV1>) {
        val event = message.payload

        publisher.publish(
            message = message,
            recipientUserIds = event.recipientUserIds,
            payload =
                AccountMemberChangedPayload(
                    accountId = message.aggregateId,
                    memberId = event.memberId,
                    userId = event.userId,
                    change = "JOINED",
                ),
        )
    }

    private companion object {
        const val ACCOUNT_SERVICE = "account-service"
    }
}
