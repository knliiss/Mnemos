package com.mnemos.notification.adapter.inbound.messaging.recipient

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.UserCreatedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.notification.application.recipient.usecase.upsert.UpsertRecipientCommand
import com.mnemos.notification.application.recipient.usecase.upsert.UpsertRecipientUseCase
import org.springframework.stereotype.Component

@Component
class UserCreatedEventHandler(
    private val upsertRecipientUseCase: UpsertRecipientUseCase,
) : KafkaMessageHandler<UserCreatedV1> {
    override val definition: KafkaMessageDefinition<UserCreatedV1> =
        kafkaMessageDefinition(
            topic = AccountEventContract.TOPIC,
            type = AccountEventType.USER_CREATED,
            version = AccountEventContract.VERSION,
            aggregateType = AccountEventAggregateType.USER_AGGREGATE_TYPE,
        )

    override val consumer = "notification-service.user-created"

    override fun handle(message: ConsumedMessage<UserCreatedV1>) {
        upsertRecipientUseCase.execute(
            UpsertRecipientCommand(
                userId = UserId(message.aggregateId),
                telegramId = TelegramId(message.payload.telegramId),
            ),
        )
    }
}
