package com.mnemos.notification.adapter.inbound.messaging.account

import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class AccountEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [AccountEventContract.TOPIC],
        groupId = "\${spring.application.name}-account-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
