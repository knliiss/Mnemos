package com.mnemos.messaging.kafka.consumer

class KafkaMessageProtocolException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)