package com.mnemos.notification.adapter.inbound.messaging.itemdrop

import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventCommand
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType

object ItemDroppedEventMapper {
    fun ConsumedMessage<ItemDroppedV1>.toCommand(): ProcessItemDropEventCommand =
        ProcessItemDropEventCommand(
            messageId = aggregateId,
            occurredAt = occurredAt,
            droppedFor = payload.droppedFor,
            itemKey = payload.itemKey,
            itemName = payload.itemName,
            itemRarity = payload.itemRarity.toDomain(),
            itemType = payload.itemType.toDomain(),
        )

    private fun ContractItemType.toDomain(): NotificationItemType = NotificationItemType.valueOf(name)

    private fun ContractItemRarity.toDomain(): NotificationItemRarity = NotificationItemRarity.valueOf(name)
}
