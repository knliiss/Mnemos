package com.mnemos.social.adapter.out.http.event

import com.mnemos.social.application.spotlight.port.out.ItemDropLookup
import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ItemDropLookupHttpAdapter(
    private val client: EventServiceClient,
) : ItemDropLookup {
    override fun findById(itemDropEventId: UUID): ItemDropSnapshot? =
        client.getItemDrop(itemDropEventId)?.let { response ->
            ItemDropSnapshot(
                id = response.id,
                itemId = response.itemId,
                itemName = response.itemName,
                itemRarity = response.itemRarity,
                itemType = response.itemType,
                droppedFor = response.droppedFor,
                occurredAt = response.occurredAt,
            )
        }
}
