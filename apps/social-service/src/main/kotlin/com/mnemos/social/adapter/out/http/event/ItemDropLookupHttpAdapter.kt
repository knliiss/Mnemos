package com.mnemos.social.adapter.out.http.event

import com.mnemos.social.application.spotlight.model.ItemDropSnapshot
import com.mnemos.social.application.spotlight.port.out.ItemDropLookup
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ItemDropLookupHttpAdapter(
    private val client: EventServiceClient,
) : ItemDropLookup {
    override fun findById(itemDropEventId: UUID): ItemDropSnapshot? =
        try {
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
        } catch (exception: FeignException.NotFound) {
            null
        }
}
