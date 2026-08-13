package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventJpaRepository
import com.mnemos.event.application.itemdrop.model.FirstItemDropView
import com.mnemos.event.application.itemdrop.port.out.FirstItemDropReader
import com.mnemos.event.application.itemdrop.usecase.first.FindFirstItemDropQuery
import org.springframework.stereotype.Repository

@Repository
class FirstItemDropQueryJpaAdapter(
    private val itemDropEventJpaRepository: ItemDropEventJpaRepository,
) : FirstItemDropReader {
    override fun findFirst(query: FindFirstItemDropQuery): FirstItemDropView? =
        itemDropEventJpaRepository
            .findFirstInRange(
                itemName = query.itemName,
                itemType = query.itemType.name,
                from = query.from,
                to = query.to,
            )?.let { projection ->
                FirstItemDropView(
                    droppedFor = projection.getDroppedFor(),
                    occurredAt = projection.getOccurredAt(),
                )
            }
}
