package com.mnemos.realtime.application.delivery.usecase.publish

import com.mnemos.realtime.application.delivery.model.RealtimeEvent
import com.mnemos.realtime.application.delivery.model.eventType
import com.mnemos.realtime.application.delivery.port.out.RealtimeDeliveryTelemetry
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventFanout
import com.mnemos.realtime.application.delivery.port.out.RealtimeEventReplayStore
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class PublishRealtimeEventService(
    private val replayStore: RealtimeEventReplayStore,
    private val fanout: RealtimeEventFanout,
    private val telemetry: RealtimeDeliveryTelemetry,
    private val clock: Clock,
) : PublishRealtimeEventUseCase {
    override fun execute(command: PublishRealtimeEventCommand): RealtimeEvent {
        val event =
            RealtimeEvent(
                id = command.eventId,
                userId = command.userId,
                type = command.payload.eventType(),
                occurredAt = command.occurredAt,
                operationId = command.operationId,
                payload = command.payload,
            )

        val startedAt = clock.instant()

        try {
            replayStore.append(event)
            fanout.publish(event)
        } catch (exception: Exception) {
            telemetry.recordPublishFailure(
                event = event,
                duration = elapsedSince(startedAt),
            )

            throw exception
        }

        telemetry.recordPublishSuccess(
            event = event,
            duration = elapsedSince(startedAt),
        )

        return event
    }

    private fun elapsedSince(startedAt: Instant): Duration =
        Duration
            .between(startedAt, clock.instant())
            .coerceAtLeast(Duration.ZERO)
}
