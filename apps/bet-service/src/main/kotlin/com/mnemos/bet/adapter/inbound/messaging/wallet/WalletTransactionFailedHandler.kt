package com.mnemos.bet.adapter.inbound.messaging.wallet

import com.mnemos.bet.application.wallet.usecase.process.ProcessWalletTransactionFailedCommand
import com.mnemos.bet.application.wallet.usecase.process.ProcessWalletTransactionFailedUseCase
import com.mnemos.bet.domain.wallet.model.WalletRequestId
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import com.mnemos.wallet.contracts.wallet.WalletEventType
import com.mnemos.wallet.contracts.wallet.event.WalletTransactionFailedEvent
import org.springframework.stereotype.Component

@Component
class WalletTransactionFailedHandler(
    private val useCase: ProcessWalletTransactionFailedUseCase,
) : KafkaMessageHandler<WalletTransactionFailedEvent> {
    override val definition =
        kafkaMessageDefinition<WalletTransactionFailedEvent>(
            WalletEventContract.TOPIC,
            WalletEventType.TRANSACTION_FAILED,
            WalletEventContract.VERSION,
            WalletEventContract.AGGREGATE_TYPE,
        )

    override val consumer = "bet-service.wallet-failed"

    override fun handle(message: ConsumedMessage<WalletTransactionFailedEvent>) {
        useCase.execute(
            ProcessWalletTransactionFailedCommand(
                requestId = WalletRequestId(message.payload.requestId),
            ),
        )
    }
}
