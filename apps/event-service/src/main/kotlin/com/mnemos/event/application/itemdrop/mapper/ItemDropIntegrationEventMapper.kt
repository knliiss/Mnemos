package com.mnemos.event.application.itemdrop.mapper

import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.contracts.contract.v1.ItemDroppedV1
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import com.mnemos.event.domain.drop.model.ItemDropEvent
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.Item
import com.mnemos.messaging.core.event.IntegrationEvent

object ItemDropIntegrationEventMapper {
    fun ItemDropEvent.toIntegrationEvent(item: Item): IntegrationEvent<ItemDroppedV1> =
        IntegrationEvent(
            type = EventEventType.ITEM_DROPPED,
            aggregateId = id.id,
            occurredAt = occurredAt,
            payload =
                ItemDroppedV1(
                    itemId = item.id.id,
                    itemKey = item.gameKey,
                    itemName = item.name,
                    itemType = item.type.toContract(),
                    itemRarity = item.rarity.toContract(),
                    droppedFor = droppedFor,
                    playerId = playerId?.id,
                ),
        )

    private fun DomainItemType.toContract(): ContractItemType = ContractItemType.valueOf(name)

    private fun DomainItemRarity.toContract(): ContractItemRarity = ContractItemRarity.valueOf(name)
}
