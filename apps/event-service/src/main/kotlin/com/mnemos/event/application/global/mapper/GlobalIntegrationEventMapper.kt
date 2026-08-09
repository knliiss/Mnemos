package com.mnemos.event.application.global.mapper

import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.GlobalStartedV1
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.global.model.GlobalEvent
import com.mnemos.messaging.core.event.IntegrationEvent

object GlobalIntegrationEventMapper {
    fun GlobalEvent.toIntegrationEvent(): IntegrationEvent<GlobalStartedV1> =
        IntegrationEvent(
            type = EventEventType.GLOBAL_STARTED,
            aggregateId = id.id,
            occurredAt = startedAt,
            payload =
                GlobalStartedV1(
                    type = type.toContract(),
                ),
        )

    private fun DomainGlobalEventType.toContract(): ContractGlobalEventType = ContractGlobalEventType.valueOf(name)
}
