package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.telegram.bot.adapter.out.event.EventHistoryMapper.toModel
import com.mnemos.telegram.bot.application.events.model.EventDetailResult
import com.mnemos.telegram.bot.application.events.port.out.EventDetailGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EventDetailGatewayAdapter(
    private val client: EventServiceClient,
) : EventDetailGateway {
    override fun get(eventId: UUID): EventDetailResult =
        try {
            EventDetailResult.Available(client.getEvent(eventId).toModel())
        } catch (exception: FeignException) {
            if (exception.status() == 404) EventDetailResult.NotFound
            else EventDetailResult.Unavailable
        } catch (_: RuntimeException) {
            EventDetailResult.Unavailable
        }
}
