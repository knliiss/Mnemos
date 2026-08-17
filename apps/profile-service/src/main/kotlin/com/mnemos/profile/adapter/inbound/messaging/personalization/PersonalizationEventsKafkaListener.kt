package com.mnemos.profile.adapter.inbound.messaging.personalization

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.personalization.contracts.PersonalizationEventContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PersonalizationEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [PersonalizationEventContract.TOPIC],
        groupId = "\${spring.application.name}-personalization-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
