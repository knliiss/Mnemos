package com.mnemos.notification.adapter.inbound.messaging.global

import com.mnemos.event.contracts.contract.v1.GlobalStartedV1
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.notification.application.global.usecase.process.ProcessGlobalEventCommand
import com.mnemos.notification.domain.global.model.DomainGlobalEventType

object GlobalStartedEventMapper {
    fun ConsumedMessage<GlobalStartedV1>.toCommand(): ProcessGlobalEventCommand =
        ProcessGlobalEventCommand(
            messageId = messageId,
            occurredAt = occurredAt,
            eventType = payload.type.toDomain(),
        )

    private fun ContractGlobalEventType.toDomain(): DomainGlobalEventType =
        when (this) {
            ContractGlobalEventType.DARKNESS -> DomainGlobalEventType.DARKNESS
            ContractGlobalEventType.MOON -> DomainGlobalEventType.MOON
            ContractGlobalEventType.ECLIPSE -> DomainGlobalEventType.ECLIPSE
            ContractGlobalEventType.EXPLOSION -> DomainGlobalEventType.EXPLOSION
        }
}
