package com.mnemos.profile.adapter.inbound.messaging.social

import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.social.contracts.SocialEventContract
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SocialEventsKafkaListener(
    private val dispatcher: KafkaMessageDispatcher,
) {
    @KafkaListener(
        topics = [SocialEventContract.TOPIC],
        groupId = "\${spring.application.name}-social-events",
    )
    fun handle(record: ConsumerRecord<String, String>) {
        dispatcher.dispatch(record)
    }
}
