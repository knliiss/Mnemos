package com.mnemos.personalization.adapter.inbound.messaging.wallet

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.wallet.contracts.wallet.WalletEventContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class WalletEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [WalletEventContract.TOPIC],
        groupId = "\${spring.application.name}-wallet-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
