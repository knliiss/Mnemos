package com.mnemos.wallet.adapter.inbound.messaging.transaction

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.wallet.application.transaction.mapper.WalletCreateTransactionCommandMapper.toRequest
import com.mnemos.wallet.application.transaction.usecase.create.CreateTransactionResult
import com.mnemos.wallet.application.transaction.usecase.create.ProcessCreateTransactionCommandService
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import com.mnemos.wallet.contracts.wallet.WalletCommandType
import com.mnemos.wallet.contracts.wallet.command.WalletCreateTransactionCommand
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import org.springframework.stereotype.Component

@Component
class WalletCreateTransactionCommandHandler(
    private val processCreateTransactionCommandService: ProcessCreateTransactionCommandService,
) : KafkaMessageHandler<WalletCreateTransactionCommand> {
    override val definition: KafkaMessageDefinition<WalletCreateTransactionCommand> =
        kafkaMessageDefinition(
            topic = WalletCommandContract.TOPIC,
            type = WalletCommandType.CREATE_TRANSACTION,
            version = WalletCommandContract.VERSION,
            aggregateType = WalletCommandContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.aggregateId != envelope.payload.requestId) {
                throw KafkaMessageProtocolException(
                    "Wallet create transaction command aggregateId must match requestId",
                )
            }
        }

    override val consumer = "wallet-service.create-transaction-command"

    override fun handle(message: ConsumedMessage<WalletCreateTransactionCommand>) {
        val command = message.payload
        val result =
            processCreateTransactionCommandService.process(
                command = command,
                request = command.toRequest(),
                producer = message.producer,
                metadata =
                    EventMetadata(
                        correlationId = message.metadata.correlationId ?: command.requestId.toString(),
                        causationId = message.messageId,
                        actorUserId = message.metadata.actorUserId,
                    ),
            )

        if (
            result is CreateTransactionResult.Failed &&
            result.reason == WalletTransactionFailureReason.IDEMPOTENCY_CONFLICT
        ) {
            throw KafkaMessageProtocolException(
                "Wallet requestId was reused with a different transaction payload",
            )
        }
    }
}
