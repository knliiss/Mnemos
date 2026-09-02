package com.mnemos.telegram.bot.application.events.port.out

import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeMetadataResult

fun interface EventWipeMetadataGateway {
    fun getOptions(): EventHistoryWipeMetadataResult
}
