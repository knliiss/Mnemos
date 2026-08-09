package com.mnemos.messaging.kafka.config

import com.mnemos.messaging.inbox.InboxEventProcessor
import com.mnemos.messaging.kafka.consumer.KafkaDeadLetterTopic
import com.mnemos.messaging.kafka.consumer.KafkaMessageDispatcher
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.serialization.KafkaEventEnvelopeReader
import org.apache.kafka.common.TopicPartition
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries

@AutoConfiguration(
    after = [MnemosKafkaAutoConfiguration::class],
    afterName = ["com.mnemos.messaging.inbox.config.MnemosInboxAutoConfiguration"],
)
@ConditionalOnProperty(
    prefix = "mnemos.kafka",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class MnemosKafkaConsumerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommonErrorHandler::class)
    fun mnemosKafkaCommonErrorHandler(
        kafkaTemplate: KafkaTemplate<String, String>,
        properties: MnemosKafkaProperties,
    ): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { record, _ ->
            TopicPartition(
                KafkaDeadLetterTopic.name(record.topic()),
                record.partition(),
            )
        }.apply {
            setFailIfSendResultIsError(true)
        }

        val retry = properties.consumer.retry
        val backOff = ExponentialBackOffWithMaxRetries(retry.maxAttempts - 1).apply {
            initialInterval = retry.initialBackoff.toMillis()
            multiplier = retry.multiplier
            maxInterval = retry.maxBackoff.toMillis()
        }

        return DefaultErrorHandler(recoverer, backOff).apply {
            addNotRetryableExceptions(KafkaMessageProtocolException::class.java)
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = ["com.mnemos.messaging.inbox.InboxEventProcessor"])
    class InboxDispatcherConfiguration {

        @Bean
        @ConditionalOnBean(KafkaMessageHandler::class)
        @ConditionalOnMissingBean(KafkaMessageDispatcher::class)
        fun kafkaMessageDispatcher(
            envelopeReader: KafkaEventEnvelopeReader,
            inboxEventProcessor: InboxEventProcessor,
            handlers: List<KafkaMessageHandler<*>>,
        ): KafkaMessageDispatcher = KafkaMessageDispatcher(
            envelopeReader = envelopeReader,
            inboxEventProcessor = inboxEventProcessor,
            handlers = handlers,
        )
    }
}