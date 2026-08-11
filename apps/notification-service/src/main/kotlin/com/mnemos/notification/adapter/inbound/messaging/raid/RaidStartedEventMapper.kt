package com.mnemos.notification.adapter.inbound.messaging.raid

import com.mnemos.event.contracts.contract.v1.RaidStartedV1
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.notification.application.raid.usecase.process.ProcessRaidEventCommand

object RaidStartedEventMapper {
    fun ConsumedMessage<RaidStartedV1>.toCommand(): ProcessRaidEventCommand =
        ProcessRaidEventCommand(
            messageId = messageId,
            occurredAt = occurredAt,
            locations = payload.locations,
        )
}
