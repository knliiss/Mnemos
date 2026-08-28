package com.mnemos.notification.adapter.inbound.messaging.booster

import com.mnemos.event.contracts.contract.v1.BoosterActivatedV1
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.notification.application.booster.usecase.process.ProcessBoosterEventCommand
import com.mnemos.notification.domain.booster.model.DomainBoosterType

object BoosterActivatedEventMapper {
    fun ConsumedMessage<BoosterActivatedV1>.toCommand(): ProcessBoosterEventCommand =
        ProcessBoosterEventCommand(
            messageId = aggregateId,
            occurredAt = occurredAt,
            boosterType = payload.type.toDomain(),
            activatedBy = payload.activatedBy,
        )

    private fun ContractBoosterType.toDomain(): DomainBoosterType =
        when (this) {
            ContractBoosterType.LUCK -> DomainBoosterType.LUCK
            ContractBoosterType.MONEY -> DomainBoosterType.MONEY
            ContractBoosterType.DAMAGE -> DomainBoosterType.DAMAGE
            ContractBoosterType.POWER -> DomainBoosterType.POWER
        }
}
