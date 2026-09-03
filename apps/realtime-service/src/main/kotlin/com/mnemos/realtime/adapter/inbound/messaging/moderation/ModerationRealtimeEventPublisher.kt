package com.mnemos.realtime.adapter.inbound.messaging.moderation

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.realtime.application.delivery.model.RealtimeEventPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventCommand
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ModerationRealtimeEventPublisher(
    private val publishRealtimeEventUseCase: PublishRealtimeEventUseCase,
) {
    fun publish(
        message: ConsumedMessage<*>,
        userId: UUID,
        payload: RealtimeEventPayload,
    ) {
        publishRealtimeEventUseCase.execute(
            PublishRealtimeEventCommand(
                eventId = message.messageId,
                userId = userId,
                occurredAt = message.occurredAt,
                operationId = null,
                payload = payload,
            ),
        )
    }
}
