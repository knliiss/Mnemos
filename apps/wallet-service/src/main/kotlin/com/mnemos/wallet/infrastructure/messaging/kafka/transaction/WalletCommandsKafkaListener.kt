package com.mnemos.wallet.infrastructure.messaging.kafka.transaction

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.messaging.kafka.consumer.UnknownKafkaMessagePolicy
import com.mnemos.wallet.contracts.wallet.WalletCommandContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class WalletCommandsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [WalletCommandContract.TOPIC],
        groupId = "\${spring.application.name}-commands",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(
            record = record,
            unknownMessagePolicy = UnknownKafkaMessagePolicy.REJECT,
        )
    }
}
