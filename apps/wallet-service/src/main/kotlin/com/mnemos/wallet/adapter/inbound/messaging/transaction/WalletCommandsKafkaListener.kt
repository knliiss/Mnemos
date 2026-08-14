package com.mnemos.wallet.adapter.inbound.messaging.transaction

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
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
        groupId = "\${spring.application.name}-wallet-commands",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
