package com.mnemos.event.application.raid.mapper

import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.RaidStartedV1
import com.mnemos.event.domain.raid.model.RaidEvent
import com.mnemos.messaging.core.event.IntegrationEvent

object RaidIntegrationEventMapper {
    fun RaidEvent.toIntegrationEvent(): IntegrationEvent<RaidStartedV1> =
        IntegrationEvent(
            type = EventEventType.RAID_STARTED,
            aggregateId = id.id,
            occurredAt = startedAt,
            payload =
                RaidStartedV1(
                    locations = locations,
                ),
        )
}
