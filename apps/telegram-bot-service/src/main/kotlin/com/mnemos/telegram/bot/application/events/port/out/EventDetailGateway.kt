package com.mnemos.telegram.bot.application.events.port.out

import com.mnemos.telegram.bot.application.events.model.EventDetailResult
import java.util.UUID

fun interface EventDetailGateway {
    fun get(eventId: UUID): EventDetailResult
}
