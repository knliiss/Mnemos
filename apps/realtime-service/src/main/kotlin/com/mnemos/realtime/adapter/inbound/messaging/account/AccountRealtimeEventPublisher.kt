package com.mnemos.realtime.adapter.inbound.messaging.account

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.realtime.application.delivery.model.RealtimeEventPayload
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventCommand
import com.mnemos.realtime.application.delivery.usecase.publish.PublishRealtimeEventUseCase
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountRealtimeEventPublisher(
    private val publishRealtimeEventUseCase: PublishRealtimeEventUseCase,
) {
    fun publish(
        message: ConsumedMessage<*>,
        recipientUserIds: List<UUID>,
        payload: RealtimeEventPayload,
    ) {
        recipientUserIds.distinct().forEach { userId ->
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
}
