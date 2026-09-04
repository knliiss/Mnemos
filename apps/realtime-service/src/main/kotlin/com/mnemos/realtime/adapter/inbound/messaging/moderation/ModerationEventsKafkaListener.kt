package com.mnemos.realtime.adapter.inbound.messaging.moderation

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.moderation.contracts.ModerationEventContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ModerationEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [ModerationEventContract.TOPIC],
        groupId = "\${spring.application.name}-moderation-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
