package com.mnemos.realtime.adapter.inbound.messaging.bet

import com.mnemos.bet.contracts.BetEventContract
import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class BetEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [BetEventContract.TOPIC],
        groupId = "\${spring.application.name}-bet-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
