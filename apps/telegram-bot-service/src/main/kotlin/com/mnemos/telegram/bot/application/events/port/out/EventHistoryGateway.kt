package com.mnemos.telegram.bot.application.events.port.out

import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult

fun interface EventHistoryGateway {
    fun getPage(query: EventHistoryQuery): EventHistoryResult
}
