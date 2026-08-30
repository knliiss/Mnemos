package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.telegram.bot.adapter.out.event.EventWipeMetadataMapper.toOptions
import com.mnemos.telegram.bot.application.events.model.EventHistoryWipeMetadataResult
import com.mnemos.telegram.bot.application.events.port.out.EventWipeMetadataGateway
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventWipeMetadataGatewayAdapter(
    private val client: EventServiceClient,
) : EventWipeMetadataGateway {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getOptions(): EventHistoryWipeMetadataResult =
        try {
            EventHistoryWipeMetadataResult.Available(client.listWipes().toOptions())
        } catch (exception: RuntimeException) {
            logger.warn("Could not load wipe metadata from event-service", exception)
            EventHistoryWipeMetadataResult.Unavailable
        }
}
