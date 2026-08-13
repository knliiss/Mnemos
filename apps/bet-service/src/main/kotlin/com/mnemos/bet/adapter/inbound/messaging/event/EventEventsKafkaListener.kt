package com.mnemos.bet.adapter.inbound.messaging.event

import com.mnemos.event.contracts.EventEventContract
import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class EventEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [EventEventContract.TOPIC],
        groupId = "\${spring.application.name}-event-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
