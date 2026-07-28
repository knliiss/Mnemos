package com.mnemos.messaging.core.transport

import java.util.concurrent.CompletionStage

interface EventTransport {
    fun publish(message: EventMessage): CompletionStage<Unit>
}