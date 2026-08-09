package com.mnemos.event.application.booster.mapper

import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.BoosterActivatedV1
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.domain.booster.model.BoosterEvent
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.messaging.core.event.IntegrationEvent

object BoosterIntegrationEventMapper {
    fun BoosterEvent.toIntegrationEvent(): IntegrationEvent<BoosterActivatedV1> =
        IntegrationEvent(
            type = EventEventType.RAID_STARTED,
            aggregateId = id.id,
            occurredAt = activatedAt,
            payload =
                BoosterActivatedV1(
                    activatedBy = activatedBy,
                    type = type.toContract(),
                ),
        )

    private fun DomainBoosterType.toContract(): ContractBoosterType = ContractBoosterType.valueOf(name)
}
